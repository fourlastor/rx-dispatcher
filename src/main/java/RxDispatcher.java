import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
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
                    return r.path.equals(request.getPath());
                }
            })
            .flatMap(new Func1<Response, Observable<Void>>() {
                @Override
                public Observable<Void> call(Response r) {
                    response.writeString(r.body);
                    response.setStatus(HttpResponseStatus.OK);
                    return response.close();
                }
            });
    }

    public void match(String path, String body) {
        subject.onNext(new Response(path, body));
    }

    static class Response {
        final String path;
        final String body;

        Response(String path, String body) {
            this.path = path;
            this.body = body;
        }
    }
}
