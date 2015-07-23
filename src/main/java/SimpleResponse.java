import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

public class SimpleResponse implements RxDispatcher.Response {
    private final String path;
    private final String body;

    public SimpleResponse(String path, String body) {
        this.path = path;
        this.body = body;
    }

    @Override
    public boolean match(HttpServerRequest<ByteBuf> request) {
        return path.equals(request.getPath());
    }

    @Override
    public void process(HttpServerResponse<ByteBuf> response) {
        response.setStatus(HttpResponseStatus.OK);
        response.writeString(body);
    }
}
