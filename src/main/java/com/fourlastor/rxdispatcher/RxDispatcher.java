package com.fourlastor.rxdispatcher;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class RxDispatcher implements RequestHandler<ByteBuf, ByteBuf> {

    PublishSubject<Response> subject = PublishSubject.create();

    @Override
    public Observable<Void> handle(
        final HttpServerRequest<ByteBuf> request,
        final HttpServerResponse<ByteBuf> response
    ) {
        return subject
            .filter(new Func1<Response, Boolean>() {
                @Override
                public Boolean call(Response r) {
                    return r.match(request);
                }
            })
            .flatMap(new Func1<Response, Observable<Void>>() {
                @Override
                public Observable<Void> call(Response r) {
                    r.process(response);
                    return response.close();
                }
            });
    }

    public void match(String path, String body) {
        match(new SimpleResponse(path, body));
    }

    public void match(Response response) {
        subject.onNext(response);
    }

    public interface Response {
        boolean match(HttpServerRequest<ByteBuf> request);
        void process(HttpServerResponse<ByteBuf> response);
    }
}
