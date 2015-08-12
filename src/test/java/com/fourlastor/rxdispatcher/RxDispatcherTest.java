package com.fourlastor.rxdispatcher;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class RxDispatcherTest {
    private static int PORT = 8080;

    private HttpServer<ByteBuf, ByteBuf> server;
    private RxDispatcher rxDispatcher;
    private OkHttpClient client;

    @Before
    public void setUp() throws Exception {
        client = new OkHttpClient();
        rxDispatcher = new RxDispatcher();
        server = RxNetty.createHttpServer(++PORT, rxDispatcher);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void noMatch() throws Exception {
        TestSubscriber<Response> subscriber = request("/hello");

        subscriber.awaitTerminalEvent(500, TimeUnit.MILLISECONDS);
        subscriber.assertNotCompleted();
    }

    @Test
    public void match() throws Exception {
        TestSubscriber<Response> subscriber = request("/hello");
        rxDispatcher.match("/hello", "world");

        matchesBody(subscriber, "world");
    }

    @Test
    public void matchAndNoMatch() throws Exception {
        TestSubscriber<Response> missing = request("/notMatched");
        TestSubscriber<Response> subscriber = request("/hello");

        rxDispatcher.match("/hello", "world");

        matchesBody(subscriber, "world");

        missing.assertNotCompleted();
    }

    @Test
    public void matchCustomResponse() throws Exception {
        TestSubscriber<Response> subscriber = request("/hello");

        rxDispatcher.match(new RxDispatcher.Response() {
            @Override
            public boolean match(HttpServerRequest<ByteBuf> request) {
                return true;
            }

            @Override
            public void process(HttpServerResponse<ByteBuf> response) {
                response.setStatus(HttpResponseStatus.OK);
                response.writeString("world");
            }
        });

        matchesBody(subscriber, "world");
    }

    private void matchesBody(TestSubscriber<Response> subscriber, String body) throws IOException {
        subscriber.awaitTerminalEvent(500, TimeUnit.MILLISECONDS);
        subscriber.assertCompleted();

        Response response = subscriber.getOnNextEvents().get(0);
        assertEquals(body, response.body().string());
    }

    private TestSubscriber<Response> request(String path) throws InterruptedException {
        Request request = new Request.Builder()
            .url("http://localhost:" + PORT + path)
            .build();

        TestSubscriber<Response> subscriber = new TestSubscriber<>();
        RxOkHttp.request(
            client,
            request
        ).subscribe(subscriber);

        Thread.sleep(500);

        return subscriber;
    }
}
