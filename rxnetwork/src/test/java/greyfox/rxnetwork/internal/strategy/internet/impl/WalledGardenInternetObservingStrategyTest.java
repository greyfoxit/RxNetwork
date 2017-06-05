package greyfox.rxnetwork.internal.strategy.internet.impl;

import static greyfox.rxnetwork.internal.strategy.internet.impl.WalledGardenInternetObservingStrategy.builder;

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
import org.junit.Test;

/**
 * @author Radek Kozak
 */
@SuppressWarnings("ConstantConditions")
public class WalledGardenInternetObservingStrategyTest
        extends EndpointInternetObservingStrategyTest {

    private static final WalledGardenInternetObservingStrategy.Builder NULL_BUILDER = null;

    // WalledGardenInternetStrategy uses HTTP Status-Code 204: No Content to validate connection
    private static final int VALID_SERVER_RESPONSE = HTTP_NO_CONTENT;

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaEmptyConstructor() {
        new WalledGardenInternetObservingStrategy();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithNullBuilder() {
        new WalledGardenInternetObservingStrategy(NULL_BUILDER);
    }

    @Test
    public void shouldSubscribeCorrectly_whenCreatedFromDefaultFactory() {
        InternetObservingStrategy sut = WalledGardenInternetObservingStrategy.create();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void shouldSubscribeCorrectly_whenCreatedFromDetailedBuilder() {
        InternetObservingStrategy sut = detailedStrategyBuilder().build();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void shouldThrow_whenTryingToObserveInvalidEndpoint() {
        InternetObservingStrategy sut = builder()
                .endpoint(INVALID_HOST).build();

        assertThat(sut.observe().blockingFirst()).isFalse();
    }

    @Test
    public void internetConnectionShouldBeTrue()
            throws IOException, InternetObservingStrategyException {

        WalledGardenInternetObservingStrategy sut = spy(detailedStrategyBuilder().build());
        HttpURLConnection urlConnection = mock(HttpURLConnection.class);
        doReturn(VALID_SERVER_RESPONSE).when(urlConnection).getResponseCode();
        doReturn(urlConnection).when(sut).buildUrlConnection(any(URL.class));

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    @Test
    public void internetConnectionShouldBeTrue_whenValidServerResponse()
            throws InterruptedException, IOException {

        setServerWithHttpStatusResponse(VALID_SERVER_RESPONSE);
        InternetObservingStrategy sut = buildStrategy();

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    @Test
    public void shouldLogError_whenProblemGettingResponseCode() throws IOException,
            InternetObservingStrategyException {

        WalledGardenInternetObservingStrategy sut = spy(detailedStrategyBuilder().build());
        HttpURLConnection urlConnection = mock(HttpURLConnection.class);
        doThrow(IOException.class).when(urlConnection).getResponseCode();

        doReturn(urlConnection).when(sut).buildUrlConnection(any(URL.class));

        assertThat(sut.observe().blockingFirst()).isFalse();
        verify(sut).onError(anyString(), any(Exception.class));
    }

    @Test
    public void internetConnectionShouldBeFalse_whenInvalidServerResponse() throws IOException {

        setServerWithHttpStatusResponse(INVALID_SERVER_RESPONSE);
        InternetObservingStrategy sut = buildStrategy();

        assertThat(sut.observe().blockingFirst()).isFalse();
    }

    private WalledGardenInternetObservingStrategy.Builder detailedStrategyBuilder() {
        return builder().delay(VALID_DELAY).interval(VALID_INTERVAL).timeout(VALID_TIMEOUT_MS)
                .endpoint(VALID_ENDPOINT);
    }

    @Override
    protected EndpointInternetObservingStrategy.Builder strategyBuilder() {
        return builder();
    }
}
