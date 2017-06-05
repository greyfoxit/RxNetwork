/*
 * Copyright (C) 2017 Greyfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greyfox.rxnetwork.internal.strategy.internet.impl;

import static greyfox.rxnetwork.internal.strategy.internet.impl.Http200InternetObservingStrategy.builder;

import static java.net.HttpURLConnection.HTTP_OK;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Radek Kozak
 */
@SuppressWarnings({"ConstantConditions"})
@RunWith(MockitoJUnitRunner.class)
public class Http200InternetObservingStrategyTest extends EndpointInternetObservingStrategyTest {

    private static final Http200InternetObservingStrategy.Builder NULL_BUILDER = null;

    // Http200InternetObservingStrategy uses HTTP Status-Code 200: OK to validate connection
    private static final int VALID_SERVER_RESPONSE = HTTP_OK;

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaEmptyConstructor() {
        new Http200InternetObservingStrategy();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithNullBuilder() {
        new Http200InternetObservingStrategy(NULL_BUILDER);
    }

    @Test
    public void shouldSubscribeCorrectly() {
        InternetObservingStrategy sut = Http200InternetObservingStrategy.create();

        sut.observe().test().assertSubscribed();
    }

    @Test
    public void internetConnectionShouldBeTrue()
            throws InternetObservingStrategyException, IOException {

        Http200InternetObservingStrategy sut = spy(detailedStrategyBuilder().build());
        HttpURLConnection urlConnection = mock(HttpURLConnection.class);
        doReturn(VALID_SERVER_RESPONSE).when(urlConnection).getResponseCode();
        doReturn(urlConnection).when(sut).buildUrlConnection(any(URL.class));

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    @Test
    public void internetConnectionShouldBeFalse_whenTryingToObserveInvalidEndpoint() {
        InternetObservingStrategy sut = builder()
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

        setServerWithHttpStatusResponse(VALID_SERVER_RESPONSE);
        InternetObservingStrategy sut = buildStrategy();

        assertThat(sut.observe().blockingFirst()).isTrue();
    }

    @Test
    public void internetConnectionShouldBeFalse_whenInvalidServerResponse()
            throws InterruptedException, IOException {

        setServerWithHttpStatusResponse(INVALID_SERVER_RESPONSE);
        InternetObservingStrategy sut = buildStrategy();

        assertThat(sut.observe().blockingFirst()).isFalse();
    }

    private Http200InternetObservingStrategy.Builder detailedStrategyBuilder() {
        return builder().delay(VALID_DELAY).interval(VALID_INTERVAL).timeout(VALID_TIMEOUT_MS)
                .endpoint(VALID_ENDPOINT);
    }

    @Override
    protected EndpointInternetObservingStrategy.Builder strategyBuilder() {
        return builder();
    }
}
