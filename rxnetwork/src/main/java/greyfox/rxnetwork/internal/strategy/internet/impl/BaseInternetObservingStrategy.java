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
import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * Defines base internet observing strategy to use with {@link Observable#interval}.
 *
 * @author Radek Kozak
 */
abstract class BaseInternetObservingStrategy implements InternetObservingStrategy {

  private long delay;
  private long interval;

  BaseInternetObservingStrategy(@NonNull Builder builder) {
    checkNotNull(builder, "builder");

    delay = builder.delay;
    interval = builder.interval;
  }

  BaseInternetObservingStrategy() {
  }

  /**
   * The API base delay (in {@code ms})
   * used for observing with {@link Observable#interval}.
   */
  public long delay() {
    return delay;
  }

  /**
   * The API base interval (in {@code ms})
   * used for observing with {@link Observable#interval}.
   */
  public long interval() {
    return interval;
  }

  abstract Logger logger();

  private Function<Long, Boolean> toConnectionState() {
    return new Function<Long, Boolean>() {
      @Override
      public Boolean apply(Long tick) throws Exception {
        return checkConnection();
      }
    };
  }

  @Override
  public Observable<Boolean> observe() {
    return Observable.interval(delay, interval, TimeUnit.MILLISECONDS).map(toConnectionState())
                     .distinctUntilChanged();
  }

  abstract boolean checkConnection();

  void onError(String message, Exception exception) {
    logger().log(Level.WARNING,
        message + ": " + exception.getMessage() + ((exception.getCause() != null) ? ": " + exception
            .getCause().getMessage() : ""));
  }

  // @formatter:off

  @SuppressWarnings("unchecked")
  public abstract static class Builder<S extends BaseInternetObservingStrategy,
      B extends Builder<S, B>> {

    // @formatter:on

    private static final int DEFAULT_DELAY_MS = 0;
    private static final int DEFAULT_INTERVAL_MS = 3000;
    private long delay = DEFAULT_DELAY_MS;
    private long interval = DEFAULT_INTERVAL_MS;

    protected Builder() {
    }

    final B self() {
      return (B) this;
    }

    @NonNull
    public B delay(long delay) {
      this.delay = delay;
      return self();
    }

    @NonNull
    public B interval(long interval) {
      this.interval = interval;
      return self();
    }

    /**
     * Returns an immutable {@link BaseInternetObservingStrategy}
     * based on the fields set in this builder.
     */
    @NonNull
    public abstract S build();
  }
}
