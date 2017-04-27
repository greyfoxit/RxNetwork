package greyfox.rxnetwork.internal.strategy.internet.impl;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public class BuiltInInternetObservingStrategyTest {

    private static final BuiltInInternetObservingStrategy.Builder NULL_BUILDER = null;
    private static final String INVALID_HOST = "htt:/invalid.endpoint";

    // built-in strategy uses HTTP Status-Code 204: No Content to validate connection
    private static final int VALID_SERVER_RESPONSE = HTTP_NO_CONTENT;
    private static final int INVALID_SERVER_RESPONSE = HTTP_INTERNAL_ERROR;
    private static final int VALID_TIMEOUT_MS = 100;

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
    public void shouldSubscribeCorrectly_whenCreatedFromDefaultFactory() {
        InternetObservingStrategy sut = BuiltInInternetObservingStrategy.create();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void shouldSubscribeCorrectly_whenCreatedFromDetailedBuilder() {
        InternetObservingStrategy sut = detailedStrategyBuilder().build();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void shouldThrow_whenTryingToObserveInvalidEndpoint() {
        InternetObservingStrategy sut = BuiltInInternetObservingStrategy.builder()
                .endpoint(INVALID_HOST).build();

        assertThat(sut.observe().blockingFirst()).isFalse();
    }

    @Test
    public void internetConnectionShouldBeTrue()
            throws IOException, InternetObservingStrategyException {

        BuiltInInternetObservingStrategy sut = spy(detailedStrategyBuilder().build());
        HttpURLConnection urlConnection = mock(HttpURLConnection.class);
        doReturn(VALID_SERVER_RESPONSE).when(urlConnection).getResponseCode();
        doReturn(urlConnection).when(sut).buildUrlConnection(any(URL.class));

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    @Test
    public void internetConnectionShouldBeTrue_whenValidServerResponse()
            throws InterruptedException, IOException {

        setServerWithHttpStatusResponse(server, VALID_SERVER_RESPONSE);
        InternetObservingStrategy sut = buildStrategy(server);

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    @Test
    public void shouldLogError_whenProblemGettingResponseCode() throws IOException,
            InternetObservingStrategyException {

        BuiltInInternetObservingStrategy sut = spy(detailedStrategyBuilder().build());
        HttpURLConnection urlConnection = mock(HttpURLConnection.class);
        doThrow(IOException.class).when(urlConnection).getResponseCode();

        doReturn(urlConnection).when(sut).buildUrlConnection(any(URL.class));

        assertThat(sut.observe().blockingFirst()).isFalse();
        verify(sut).onError(anyString(), any(Exception.class));
    }

    @Test
    public void internetConnectionShouldBeFalse_whenInvalidServerResponse() throws IOException {

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

    private BuiltInInternetObservingStrategy.Builder detailedStrategyBuilder() {
        return BuiltInInternetObservingStrategy.builder().timeout(VALID_TIMEOUT_MS);
    }
}

