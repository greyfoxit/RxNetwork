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
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

abstract class EndpointInternetObservingStrategyTest {

  static final String INVALID_HOST = "htt:/invalid.endpoint";

  static final int INVALID_SERVER_RESPONSE = HTTP_INTERNAL_ERROR;
  static final int VALID_TIMEOUT_MS = 100;
  static final String VALID_ENDPOINT = " http://localhost";
  static final long VALID_DELAY = 500;
  static final long VALID_INTERVAL = 1000;

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

  void setServerWithHttpStatusResponse(int httpStatusCode) {
    server.enqueue(new MockResponse().setResponseCode(httpStatusCode));
  }

  InternetObservingStrategy buildStrategy() {
    String testEndpoint = server.url("/").toString();
    return strategyBuilder().endpoint(testEndpoint).build();
  }

  protected abstract EndpointInternetObservingStrategy.Builder strategyBuilder();
}

