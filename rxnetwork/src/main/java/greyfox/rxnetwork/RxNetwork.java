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
package greyfox.rxnetwork;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategyFactory;
import greyfox.rxnetwork.internal.strategy.internet.impl.WalledGardenInternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategyFactory;
import greyfox.rxnetwork.internal.strategy.network.factory.BuiltInNetworkObservingStrategyFactory;
import greyfox.rxnetwork.internal.strategy.network.providers.BuiltInNetworkObservingStrategyProviders;
import greyfox.rxnetwork.internal.strategy.network.providers.NetworkObservingStrategyProvider;
import greyfox.rxnetwork.internal.strategy.network.providers.ObservingStrategyProviders;
import io.reactivex.Observable;
import io.reactivex.Scheduler;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.INTERNET;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNullWithMessage;
import static greyfox.rxnetwork.internal.strategy.network.helpers.Functions.TO_CONNECTION_STATE;

/**
 * RxNetwork is a class that listens to network connectivity changes in a reactive manner.
 * <p>
 * Instances of RxNetwork can be created simply like this:
 * <pre><code>
 *     RxNetwork rxnetwork = RxNetwork.init(this));
 *
 *     // or event like this, if working only with internet strategies
 *     RxNetwork rxnetwork = RxNetwork.init();
 * </code></pre>
 * <p>
 * You can also configure various aspects of RxNetwork by using provided
 * {@linkplain Builder builder}.
 * <p>
 * For example,
 * <pre><code>
 *     RxNetwork rxnetwork = RxNetwork.builder()
 *         .defaultScheduler(AndroidSchedulers.mainThread())
 *         .networkObservingStrategy(new YourCustomNetworkObservingStrategy())
 *         .internetObservingStrategy(new YourCustomInternetObservingStrategy())
 *         .build();
 * </code></pre>
 * <p>
 * For more "dynamic" approach custom factories can be provided
 * <pre><code>
 *     RxNetwork rxnetwork = RxNetwork.builder()
 *         .networkObservingFactory(new YourCustomNetworkObservingFactory())
 *         .internetObservingFactory(new YourCustomInternetObservingFactory())
 *         .build();
 * }
 * </code></pre>
 * <p>
 * From Lollipop and up (API 21+) you can specify {@code defaultNetworkRequest} to be used
 * by built in network observing strategy:
 * <pre><code>
 *     NetworkRequest customRequest = new NetworkRequest.Builder()
 *         .addCapability(NET_CAPABILITY_INTERNET)
 *         .addTransportType(TRANSPORT_WIFI)
 *         .build();
 *
 *     RxNetwork.builder().defaultNetworkRequest(customRequest).build();
 * </code></pre>
 *
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
public final class RxNetwork {

  @NonNull private final NetworkObservingStrategy networkObservingStrategy;
  @NonNull private final InternetObservingStrategy internetObservingStrategy;
  @Nullable private final NetworkRequest networkRequest;
  @Nullable private final Scheduler scheduler;

  @VisibleForTesting
  RxNetwork() {
    throw new AssertionError("Use static factory methods or Builder to initialize RxNetwork");
  }

  @VisibleForTesting
  RxNetwork(@NonNull Builder builder) {
    checkNotNull(builder, "builder");

    scheduler = builder.scheduler;
    networkObservingStrategy = builder.networkObservingStrategy;
    internetObservingStrategy = builder.internetObservingStrategy;
    networkRequest = builder.networkRequest;
  }

  /** Create default implementation of RxNetwork. */
  @NonNull
  public static RxNetwork init(@NonNull Context context) {
    return builder().init(context.getApplicationContext());
  }

  /** Create default implementation of RxNetwork if not using network observing strategies. */
  @NonNull
  public static RxNetwork init() {
    return builder().init();
  }

  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  @Nullable
  @VisibleForTesting
  Scheduler scheduler() {
    return this.scheduler;
  }

  @NonNull
  @VisibleForTesting
  NetworkObservingStrategy networkObservingStrategy() {
    return this.networkObservingStrategy;
  }

  @NonNull
  @VisibleForTesting
  InternetObservingStrategy internetObservingStrategy() {
    return this.internetObservingStrategy;
  }

  @Nullable
  @VisibleForTesting
  NetworkRequest networkRequest() {
    return this.networkRequest;
  }

  /**
   * RxNetworkInfo connectivity observable with all the original {@link NetworkInfo} information.
   * <p>
   * Use this if you're interested in more than just the connection and could use
   * more information of actual network information being emitted.
   *
   * @return {@link NetworkInfo}
   */
  @NonNull
  @RequiresPermission(ACCESS_NETWORK_STATE)
  public Observable<RxNetworkInfo> observe() {
    return observe(networkObservingStrategy);
  }

  /**
   * RxNetworkInfo connectivity observable with all the original {@link NetworkInfo} information
   * that uses custom defined {@link NetworkObservingStrategy strategy}.
   *
   * @param strategy custom {@link NetworkObservingStrategy} instance
   *
   * @return {@link NetworkInfo} Observable
   */
  @NonNull
  @RequiresPermission(ACCESS_NETWORK_STATE)
  public Observable<RxNetworkInfo> observe(@NonNull NetworkObservingStrategy strategy) {
    checkNotNullWithMessage(strategy, "Please provide network observing strategy or initialize"
        + " RxNetwork with proper Context to use the default one");

    final Observable<RxNetworkInfo> observable = strategy.observe();

    if (scheduler != null) {
      observable.subscribeOn(scheduler);
    }

    return observable;
  }

  /**
   * Simple network connectivity observable based on {@linkplain #observe()}
   * that filters all the unnecessary information from {@link NetworkInfo} and shows only
   * bare connection status changes.
   * <p>
   * Use this if you don't care about all the {@link NetworkInfo} details.
   *
   * @return {@code true} if network available and connected or connecting, {@code false} if not
   */
  @NonNull
  @RequiresPermission(ACCESS_NETWORK_STATE)
  public Observable<Boolean> observeSimple() {
    return observe().map(TO_CONNECTION_STATE);
  }

  /**
   * Real internet access observable.
   *
   * @return {@code true} if there is real internet access, {@code false} otherwise
   */
  @NonNull
  @RequiresPermission(INTERNET)
  public Observable<Boolean> observeInternetAccess() {
    return observeInternetAccess(internetObservingStrategy);
  }

  /**
   * Real internet access observable with custom defined {@link InternetObservingStrategy strategy}.
   *
   * @param strategy {@link InternetObservingStrategy} instance
   *
   * @return {@code true} if there is real internet access, {@code false} otherwise
   */
  @NonNull
  @RequiresPermission(INTERNET)
  public Observable<Boolean> observeInternetAccess(@NonNull InternetObservingStrategy strategy) {
    checkNotNull(strategy, "internet observing strategy");

    final Observable<Boolean> observable = strategy.observe();
    if (scheduler != null) {
      observable.subscribeOn(scheduler);
    }
    return observable;
  }

  /**
   * Build a new {@link RxNetwork}.
   */
  public static final class Builder {

    private Scheduler scheduler;
    private NetworkObservingStrategy networkObservingStrategy;
    private InternetObservingStrategy internetObservingStrategy;
    private NetworkRequest networkRequest;

    Builder() {
    }

    /** Set default <i>"subscribeOn"</i> scheduler to be used by all library's observables. */
    public Builder defaultScheduler(@NonNull Scheduler scheduler) {
      this.scheduler = checkNotNull(scheduler, "scheduler");
      return this;
    }

    /** Set custom network observing strategy to be used by library. */
    public Builder networkObservingStrategy(@NonNull NetworkObservingStrategy strategy) {
      networkObservingStrategy = checkNotNull(strategy, "network observing strategy");
      return this;
    }

    /** Set custom network observing strategy factory to be used as library's default. */
    public Builder networkObservingStrategyFactory(
        @NonNull NetworkObservingStrategyFactory factory) {

      checkNotNull(factory, "network observing strategy factory");
      networkObservingStrategy = factory.get();
      return this;
    }

    /** Set custom internet observing strategy to be used by library. */
    public Builder internetObservingStrategy(@NonNull InternetObservingStrategy strategy) {
      this.internetObservingStrategy = checkNotNull(strategy, "internet observing strategy");
      return this;
    }

    /** Set custom internet observing strategy factory to be used as library's default. */
    public Builder internetObservingStrategyFactory(
        @NonNull InternetObservingStrategyFactory factory) {

      checkNotNull(factory, "internet observing strategy factory");
      internetObservingStrategy = factory.get();
      return this;
    }

    /**
     * Set the default {@link NetworkRequest network request} to be used
     * by network strategy when on <i>Lollipop+</i> device.
     * <p>
     * <b>This is useful and only take effect on {@code API 21+}</b>
     */
    @RequiresApi(LOLLIPOP)
    public Builder defaultNetworkRequest(@NonNull NetworkRequest networkRequest) {
      this.networkRequest = checkNotNull(networkRequest, "networkRequest");
      return this;
    }

    /**
     * Create the {@link RxNetwork} instance using the configured values.
     * <p>
     * This method should be called if <i>"network strategies part"</i> of library
     * is going to be used in any way.
     * <p>
     * This is recommended way of initializing RxNetwork
     */
    @NonNull
    public RxNetwork init(@NonNull Context context) {
      checkNotNull(context, "Cannot initialize RxNetwork with null context");

      if (networkObservingStrategy == null) {
        networkObservingStrategy = getNetworkObservingStrategy(context);
      }

      return init();
    }

    /**
     * Create the {@link RxNetwork} instance using the configured values.
     * <p>
     * This method can be used if only <i>"internet strategies part"</i> of library
     * is going to be used.
     * <p>
     * Otherwise it is recommended to call {@link #init(Context)}.
     */
    @NonNull
    public RxNetwork init() {
      if (internetObservingStrategy == null) {
        internetObservingStrategy = WalledGardenInternetObservingStrategy.create();
      }

      return new RxNetwork(this);
    }

    private NetworkObservingStrategy getNetworkObservingStrategy(@NonNull Context context) {
      final ObservingStrategyProviders<NetworkObservingStrategyProvider> providers =
          (networkRequest == null) ? new BuiltInNetworkObservingStrategyProviders(context)
                                   : new BuiltInNetworkObservingStrategyProviders(context,
                                       networkRequest);

      return networkObservingStrategy =
          BuiltInNetworkObservingStrategyFactory.create(providers).get();
    }
  }
}
