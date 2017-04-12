package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Radek Kozak
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
@RunWith(MockitoJUnitRunner.class)
public class SocketInternetObservingStrategyTest {

    int TIMEOUT_MS = 200;
    int DELAY_MS = 100;
    int INTERVAL_MS = 200;

    String INVALID_HOST = "invalid.endpoint";
    String VALID_HOST = "localhost";

    MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaEmptyConstructor() {
        new SocketInternetObservingStrategy();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithNullBuilder() {
        new SocketInternetObservingStrategy(null);
    }

    @Test
    public void shouldSubscribeCorrectly() {
        InternetObservingStrategy sut = SocketInternetObservingStrategy.create();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void shouldSubscribeCorrectly_whenCreatedFromDetailedBuilder() {
        InternetObservingStrategy sut = detailedStrategyBuilder().build();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void shouldLogError_whenProblemClosingSocket() throws IOException {
        SocketInternetObservingStrategy sut = spy(detailedStrategyBuilder().build());
        Socket socket = mock(Socket.class);
        doThrow(IOException.class).when(socket).close();

        doReturn(socket).when(sut).connectSocketTo(any(SocketAddress.class), anyInt());

        sut.observe().blockingFirst();
        verify(sut).onError(anyString(), any(Exception.class));
    }

    @Test
    public void internetConnectionShouldBeFalse_whenTryingToObserveInvalidEndpoint() {
        InternetObservingStrategy sut = detailedStrategyBuilder().endpoint(INVALID_HOST).build();

        assertThat(sut.observe().blockingFirst()).isFalse();
    }

    @Test
    public void internetConnectionShouldBeTrue_whenTryingToObserveValidEndpoint()
            throws InterruptedException, IOException {

        String host = server.url("/").host();
        int port = server.url("/").port();

        InternetObservingStrategy sut = detailedStrategyBuilder()
                .endpoint(VALID_HOST).port(port).build();

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    private SocketInternetObservingStrategy.Builder detailedStrategyBuilder() {
        return SocketInternetObservingStrategy.builder().timeout(TIMEOUT_MS).delay(DELAY_MS)
                .interval(INTERVAL_MS);
    }
}
