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
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SocketInternetObservingStrategyTest {

  private static final int TIMEOUT_MS = 200;
  private static final int DELAY_MS = 100;
  private static final int INTERVAL_MS = 200;
  private static final String INVALID_HOST = "invalid.endpoint";

  private MockWebServer server;

  @Before
  public void setUp() throws Exception {
    server = new MockWebServer();
    server.start();
  }

  @After
  public void tearDown() throws Exception {
    server.shutdown();
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
  public void shouldReturnInternetConnectionIsFalse_whenTryingToObserveInvalidEndpoint() {
    InternetObservingStrategy sut = detailedStrategyBuilder().endpoint(INVALID_HOST).build();

    assertThat(sut.observe().blockingFirst()).isFalse();
  }

  @Test
  public void shouldReturnInternetConnectionIsTrue_whenObservingValidEndpoint()
      throws InterruptedException, IOException {

    String host = server.url("/").host();
    int port = server.url("/").port();

    InternetObservingStrategy sut = detailedStrategyBuilder().endpoint(host).port(port).build();

    assertThat(sut.observe().blockingFirst()).isTrue();
  }

  private SocketInternetObservingStrategy.Builder detailedStrategyBuilder() {
    return SocketInternetObservingStrategy.builder().timeout(TIMEOUT_MS).delay(DELAY_MS)
                                          .interval(INTERVAL_MS);
  }
}
