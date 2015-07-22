import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import io.reactivex.netty.protocol.http.server.RequestHandler;
import rx.Observable;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

public class RxDispatcher implements RequestHandler<ByteBuf, ByteBuf> {

    PublishSubject<String> subject = PublishSubject.create();

    @Override
    public Observable<Void> handle(
        HttpServerRequest<ByteBuf> request,
        final HttpServerResponse<ByteBuf> response
    ) {
        return subject.flatMap(new Func1<String, Observable<Void>>() {
            @Override
            public Observable<Void> call(String body) {
                response.writeString(body);
                response.setStatus(HttpResponseStatus.OK);
                return response.close();
            }
        });
    }

    public void match(String path, String body) {
        subject.onNext(body);
    }
}
