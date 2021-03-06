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
import android.support.annotation.RestrictTo;
import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * Defines base internet observing strategy to use with {@linkplain Observable#interval}.
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

  abstract Logger logger();

  private Function<Long, Boolean> toConnectionState() {
    return new Function<Long, Boolean>() {
      @Override
      public Boolean apply(Long tick) throws Exception {
        return checkConnection();
      }
    };
  }

  /** Base observing implementation for all internet observing stategies. */
  @Override
  @RestrictTo(LIBRARY_GROUP)
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
  abstract static class Builder<S extends BaseInternetObservingStrategy,
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

    /** Set the delay for the strategy used in {@linkplain Observable#interval}. */
    @NonNull
    public B delay(long delay) {
      this.delay = delay;
      return self();
    }

    /** Set the interval for the strategy used in {@linkplain Observable#interval}. */
    @NonNull
    public B interval(long interval) {
      this.interval = interval;
      return self();
    }

    /** Create an immutable {@linkplain BaseInternetObservingStrategy} using configured values. */
    @NonNull
    public abstract S build();
  }
}
