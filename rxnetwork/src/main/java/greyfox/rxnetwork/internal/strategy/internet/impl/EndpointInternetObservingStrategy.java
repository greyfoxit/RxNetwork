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
import java.net.InetSocketAddress;
import java.net.URL;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * Defines basic endpoint-based internet observing strategy.
 * <p>
 * Endpoint is meant here in a broad sense. Depending on specific strategy implementation
 * It can pose as, for example, a canonical hostname (as in {@link InetSocketAddress})
 * or even full blown String representation of {@link URL}
 * <p>
 * You can see more of this at work in {@link WalledGardenInternetObservingStrategy}
 * and {@link Http200InternetObservingStrategy}
 *
 * @author Radek Kozak
 */
abstract class EndpointInternetObservingStrategy extends BaseInternetObservingStrategy {

  private int timeout;
  private String endpoint;

  EndpointInternetObservingStrategy(@NonNull Builder builder) {
    super(builder);

    timeout = builder.timeout;
    endpoint = builder.endpoint;
  }

  /** The API base timeout. */
  int timeout() {
    return timeout;
  }

  /** The API base endpoint. */
  String endpoint() {
    return endpoint;
  }

  // @formatter:off

  /**
   * {@code EndpointInternetObservingStrategy} builder static inner class.
   */
  abstract static class Builder<S extends EndpointInternetObservingStrategy,
      B extends EndpointInternetObservingStrategy.Builder<S, B>>
      extends BaseInternetObservingStrategy.Builder<S, B> {

    // @formatter:on

    private int timeout;
    private String endpoint;

    protected Builder() {
      super();
    }

    /**
     * Sets the {@code timeout} and returns a reference to this Builder so that the methods can
     * be chained together.
     *
     * @param timeout the {@code timeout} to set
     *
     * @return a reference to this Builder
     */
    @NonNull
    public B timeout(int timeout) {
      this.timeout = timeout;
      return self();
    }

    /**
     * Sets the {@code endpoint} and returns a reference to this Builder so that the methods can
     * be chained together.
     *
     * @param endpoint the {@code endpoint} to set
     *
     * @return a reference to this Builder
     */
    @NonNull
    public B endpoint(@NonNull String endpoint) {
      this.endpoint = checkNotNull(endpoint, "endpoint");
      return self();
    }
  }
}
