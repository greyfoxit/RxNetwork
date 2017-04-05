package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public class BuiltInInternetObservingStrategyTest {

    private static final BuiltInInternetObservingStrategy.Builder NULL_BUILDER = null;
    private static final String INVALID_HOST = "htt:/invalid.endpoint";

    // default strategy endpoint uses HTTP Status-Code 204: No Content to validate connection
    private static final int VALID_SERVER_RESPONSE = HTTP_NO_CONTENT;
    private static final int INVALID_SERVER_RESPONSE = HTTP_INTERNAL_ERROR;

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
        new BuiltInInternetObservingStrategy();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithNullBuilder() {
        new BuiltInInternetObservingStrategy(NULL_BUILDER);
    }

    @Test
    public void shouldSubscribeCorrectly() {
        InternetObservingStrategy sut = BuiltInInternetObservingStrategy.create();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void shouldThrow_whenTryingToObserveInvalidEndpoint() {
        InternetObservingStrategy sut = BuiltInInternetObservingStrategy.builder()
                .endpoint(INVALID_HOST).build();

        assertThat(sut.observe().blockingFirst()).isFalse();
    }

    @Test
    public void internetConnectionShouldBeTrue_whenValidServerResponse()
            throws InterruptedException, IOException {

        setServerWithHttpStatusResponse(server, VALID_SERVER_RESPONSE);
        InternetObservingStrategy sut = buildStrategy(server);

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    @Test
    public void internetConnectionShouldBeFalse_whenInvalidServerResponse()
            throws InterruptedException, IOException {

        setServerWithHttpStatusResponse(server, INVALID_SERVER_RESPONSE);
        InternetObservingStrategy sut = buildStrategy(server);

        assertThat(sut.observe().blockingFirst()).isFalse();
    }

    private void setServerWithHttpStatusResponse(MockWebServer server, int httpStatusCode) {
        server.enqueue(new MockResponse().setResponseCode(httpStatusCode));
    }

    private InternetObservingStrategy buildStrategy(MockWebServer server) {
        String testEndpoint = server.url("/").toString();
        return BuiltInInternetObservingStrategy.builder().endpoint(testEndpoint).build();
    }
}

