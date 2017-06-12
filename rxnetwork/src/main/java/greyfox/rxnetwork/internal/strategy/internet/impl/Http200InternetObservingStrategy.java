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

import android.support.annotation.NonNull;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;
import static java.util.logging.Logger.getLogger;

/**
 * @author Radek Kozak
 */
public final class Http200InternetObservingStrategy extends UrlConnectionInternetObservingStrategy {

  Http200InternetObservingStrategy(@NonNull Builder builder) {
    super(builder);
  }

  @NonNull
  public static Http200InternetObservingStrategy create() {
    return builder().build();
  }

  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  @Override
  boolean isConnected(@NonNull HttpURLConnection urlConnection)
      throws InternetObservingStrategyException {

    checkNotNull(urlConnection, "urlConnection");

    try {
      return urlConnection.getResponseCode() == 200;
    } catch (IOException ioe) {
      throw new InternetObservingStrategyException("Unable to check internet access", ioe);
    }
  }

  @Override
  Logger logger() {
    return getLogger(Http200InternetObservingStrategy.class.getSimpleName());
  }

  // @formatter:off

  /**
   * {@code Http200InternetObservingStrategy} builder static inner class.
   */
  public static final class Builder extends
      UrlConnectionInternetObservingStrategy.Builder<Http200InternetObservingStrategy,
          Http200InternetObservingStrategy.Builder> {

    // @formatter:on

    private static final String DEFAULT_ENDPOINT = "http://www.g.cn/blank.html";
    private static final int DEFAULT_TIMEOUT_MS = 3000;

    Builder() {
      super();
      endpoint(DEFAULT_ENDPOINT);
      timeout(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Returns a {@code Http200InternetObservingStrategy} built from the parameters
     * previously set.
     *
     * @return a {@code Http200InternetObservingStrategy} built with parameters
     * of this {@code Http200InternetObservingStrategy.Builder}
     */
    @NonNull
    @Override
    public Http200InternetObservingStrategy build() {
      return new Http200InternetObservingStrategy(this);
    }
  }
}
