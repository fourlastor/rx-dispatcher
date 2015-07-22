import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.protocol.http.server.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.observers.TestSubscriber;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class RxDispatcherTest {
    private static int PORT = 8080;

    private HttpServer<ByteBuf, ByteBuf> server;
    private TestSubscriber<Response> subscriber;
    private RxDispatcher rxDispatcher;
    private OkHttpClient client;

    @Before
    public void setUp() throws Exception {
        client = new OkHttpClient();
        rxDispatcher = new RxDispatcher();
        server = RxNetty.createHttpServer(PORT, rxDispatcher);
        subscriber = new TestSubscriber<>();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void noRequest() throws Exception {
        request("/hello");

        subscriber.awaitTerminalEvent(500, TimeUnit.MILLISECONDS);
        subscriber.assertNotCompleted();
    }

    @Test
    public void requestMatch() throws Exception {
        request("/hello");
        rxDispatcher.match("/hello", "world");

        subscriber.awaitTerminalEvent(500, TimeUnit.MILLISECONDS);
        subscriber.assertCompleted();

        Response response = subscriber.getOnNextEvents().get(0);
        assertEquals("world", response.body().string());
    }

    private void request(String path) throws InterruptedException {
        Request request = new Request.Builder()
            .url("http://localhost:" + PORT + path)
            .build();
        RxOkHttp.request(
            client,
            request
        ).subscribe(subscriber);

        Thread.sleep(500);
    }
}
