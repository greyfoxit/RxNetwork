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

import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static greyfox.rxnetwork.internal.strategy.internet.impl.HttpOkInternetObservingStrategy.builder;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@SuppressWarnings("ConstantConditions")
@RunWith(MockitoJUnitRunner.class)
public class HttpOkInternetObservingStrategyTest extends EndpointInternetObservingStrategyTest {

  // HttpOkInternetObservingStrategy uses HTTP Status-Code 200: OK to validate connection
  private static final int VALID_SERVER_RESPONSE = HTTP_OK;

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToInstantiateWithNullBuilder() {
    new HttpOkInternetObservingStrategy(null);
  }

  @Test
  public void shouldSubscribeCorrectly() {
    InternetObservingStrategy sut = HttpOkInternetObservingStrategy.create();

    sut.observe().test().assertSubscribed();
  }

  @Test
  public void shouldReturnInternetConnectionIsTrue() throws InternetObservingStrategyException, IOException {

    HttpOkInternetObservingStrategy sut = spy(detailedStrategyBuilder().build());
    HttpURLConnection urlConnection = mock(HttpURLConnection.class);
    doReturn(VALID_SERVER_RESPONSE).when(urlConnection).getResponseCode();
    doReturn(urlConnection).when(sut).buildUrlConnection(any(URL.class));

    assertThat(sut.observe().blockingFirst()).isTrue();
  }

  @Test
  public void shouldReturnInternetConnectionIsFalse_whenTryingToObserveInvalidEndpoint() {
    InternetObservingStrategy sut = builder().endpoint(INVALID_HOST).build();

    assertThat(sut.observe().blockingFirst()).isFalse();
  }

  @Test(expected = InternetObservingStrategyException.class)
  public void shouldThrowUnderlyingException_WrappedInInternetObservingStrategyException()
      throws InternetObservingStrategyException, IOException {

    HttpOkInternetObservingStrategy sut = spy(HttpOkInternetObservingStrategy.create());
    HttpURLConnection urlConnection =
        spy((HttpURLConnection) server.url("/").url().openConnection());
    doThrow(IOException.class).when(urlConnection).getResponseCode();
    sut.isConnected(urlConnection);

    sut.observe().blockingFirst();
  }

  @Test
  public void shouldReturnInternetConnectionIsTrue_whenValidServerResponse()
      throws InterruptedException, IOException {

    setServerWithHttpStatusResponse(VALID_SERVER_RESPONSE);
    InternetObservingStrategy sut = buildStrategy();

    assertThat(sut.observe().blockingFirst()).isTrue();
  }

  @Test
  public void shouldReturnInternetConnectionIsFalse_whenInvalidServerResponse()
      throws InterruptedException, IOException {

    setServerWithHttpStatusResponse(INVALID_SERVER_RESPONSE);
    InternetObservingStrategy sut = buildStrategy();

    assertThat(sut.observe().blockingFirst()).isFalse();
  }

  private HttpOkInternetObservingStrategy.Builder detailedStrategyBuilder() {
    return builder().delay(VALID_DELAY).interval(VALID_INTERVAL).timeout(VALID_TIMEOUT_MS)
        .endpoint(VALID_ENDPOINT);
  }

  @Override
  protected EndpointInternetObservingStrategy.Builder strategyBuilder() {
    return builder();
  }
}
