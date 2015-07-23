import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.netty.protocol.http.server.HttpServerRequest;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleResponseTest {

    @Mock
    HttpServerRequest<ByteBuf> request;
    @Mock
    HttpServerResponse<ByteBuf> response;

    private SimpleResponse simpleResponse;

    @Before
    public void setUp() throws Exception {
        when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
        simpleResponse = new SimpleResponse("/hello", "world");
    }

    @Test
    public void pathMatches() throws Exception {
        when(request.getHttpMethod()).thenReturn(HttpMethod.GET);
        when(request.getPath()).thenReturn("/hello");

        boolean match = simpleResponse.match(request);

        assertTrue(match);
    }

    @Test
    public void pathDoesNotMatch() throws Exception {
        when(request.getPath()).thenReturn("/world");

        boolean match = simpleResponse.match(request);

        assertFalse(match);
    }

    @Test
    public void methodMatches() throws Exception {
        when(request.getHttpMethod()).thenReturn(HttpMethod.PUT);
        when(request.getPath()).thenReturn("/hello");

        SimpleResponse response = new SimpleResponse("/hello", "world", HttpMethod.PUT);

        boolean match = response.match(request);

        assertTrue(match);
    }

    @Test
    public void methodDoesNotMatch() throws Exception {
        when(request.getHttpMethod()).thenReturn(HttpMethod.PUT);
        when(request.getPath()).thenReturn("/hello");

        SimpleResponse response = new SimpleResponse("/hello", "world", HttpMethod.GET);

        boolean match = response.match(request);

        assertFalse(match);
    }

    @Test
    public void process() throws Exception {
        simpleResponse.process(response);

        verify(response).setStatus(HttpResponseStatus.OK);
        verify(response).writeString("world");
    }
}
