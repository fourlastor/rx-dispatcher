import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;

public class SimpleResponse implements RxDispatcher.Response {
    private final String path;
    private final String body;
    private HttpMethod method;

    public SimpleResponse(String path, String body) {
        this(path, body, HttpMethod.GET);
    }

    public SimpleResponse(String path, String body, HttpMethod method) {
        this.path = path;
        this.body = body;
        this.method = method;
    }

    @Override
    public boolean match(HttpServerRequest<ByteBuf> request) {
        return path.equals(request.getPath()) && request.getHttpMethod() == method;
    }

    @Override
    public void process(HttpServerResponse<ByteBuf> response) {
        response.setStatus(HttpResponseStatus.OK);
        response.writeString(body);
    }
}
