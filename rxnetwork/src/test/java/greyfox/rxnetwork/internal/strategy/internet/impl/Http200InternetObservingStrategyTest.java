package greyfox.rxnetwork.internal.strategy.internet.impl;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import okhttp3.mockwebserver.MockResponse;
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
public class Http200InternetObservingStrategyTest {

    static final String INVALID_HOST = "htt:/invalid.endpoint";

    static final int VALID_SERVER_RESPONSE = HTTP_OK;
    static final int INVALID_SERVER_RESPONSE = HTTP_INTERNAL_ERROR;

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
        new Http200InternetObservingStrategy();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithNullBuilder() {
        new Http200InternetObservingStrategy(null);
    }

    @Test
    public void shouldSubscribeCorrectly() {
        InternetObservingStrategy sut = Http200InternetObservingStrategy.create();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void internetConnectionShouldBeTrue()
            throws InternetObservingStrategyException, IOException {

        Http200InternetObservingStrategy sut = spy(Http200InternetObservingStrategy.create());
        HttpURLConnection urlConnection = spy((HttpURLConnection) server.url("/").url()
                .openConnection());
        doReturn(VALID_SERVER_RESPONSE).when(urlConnection).getResponseCode();
        sut.isConnected(urlConnection);

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    @Test
    public void internetConnectionShouldBeFalse_whenTryingToObserveInvalidEndpoint() {
        InternetObservingStrategy sut = Http200InternetObservingStrategy.builder()
                .endpoint(INVALID_HOST).build();

        assertThat(sut.observe().blockingFirst()).isFalse();
    }

    @Test(expected = InternetObservingStrategyException.class)
    public void shouldThrow_whenConnectionError()
            throws InternetObservingStrategyException, IOException {

        Http200InternetObservingStrategy sut = spy(Http200InternetObservingStrategy.create());
        HttpURLConnection urlConnection = spy((HttpURLConnection) server.url("/").url()
                .openConnection());
        doThrow(IOException.class).when(urlConnection).getResponseCode();
        sut.isConnected(urlConnection);

        sut.observe().blockingFirst();
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

    private BuiltInInternetObservingStrategy buildStrategy(MockWebServer server) {
        String testEndpoint = server.url("/").toString();
        return Http200InternetObservingStrategy.builder().endpoint(testEndpoint).build();
    }
}
