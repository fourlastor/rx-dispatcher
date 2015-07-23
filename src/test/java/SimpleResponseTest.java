import io.netty.buffer.ByteBuf;
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
        simpleResponse = new SimpleResponse("/hello", "world");
    }

    @Test
    public void pathMatches() throws Exception {
        when(request.getPath()).thenReturn("/hello");

        boolean matches = simpleResponse.match(request);

        assertTrue(matches);
    }

    @Test
    public void pathDoesNotMatch() throws Exception {
        when(request.getPath()).thenReturn("/world");

        boolean matches = simpleResponse.match(request);

        assertFalse(matches);
    }

    @Test
    public void process() throws Exception {
        simpleResponse.process(response);

        verify(response).setStatus(HttpResponseStatus.OK);
        verify(response).writeString("world");
    }
}
