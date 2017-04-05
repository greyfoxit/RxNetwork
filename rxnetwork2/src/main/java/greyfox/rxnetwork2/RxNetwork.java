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
package greyfox.rxnetwork2;

import static android.support.annotation.VisibleForTesting.PRIVATE;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;
import static greyfox.rxnetwork2.internal.strategy.network.helpers.Functions.TO_CONNECTION_STATE;

import android.app.Application;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.internet.impl.BuiltInInternetObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategyFactory;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategyProvider;
import greyfox.rxnetwork2.internal.strategy.network.factory.BuiltInStrategyFactory;
import greyfox.rxnetwork2.internal.strategy.network.providers.BuiltInNetworkObservingStrategyProviders;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import java.util.Collection;

/**
 * RxNetwork is a class that listens to network connectivity changes in a reactive manner.
 * It uses default {@link BuiltInStrategyFactory factory} under the hood to
 * provide concrete, api-level dependent {@link NetworkObservingStrategy strategy}
 * for observing network connectivity changes.
 * <p>
 * Custom factory can be used by simply implementing {@link NetworkObservingStrategyFactory}
 * and registering it with {@linkplain RxNetwork}:
 * <pre><code>
 * public class ExampleApplication extends Application {
 *
 *   {@literal @}Override
 *    public void onCreate() {
 *        super.onCreate();
 *
 *        // initialize like this
 *        RxNetwork.init(this));
 *
 *        // or like this - passing default defaultScheduler
 *        RxNetwork.init(this, Schedulers.io());
 *    }
 * }
 * </code></pre>
 *
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
public final class RxNetwork {

    @Nullable private final Scheduler scheduler;
    @NonNull private final NetworkObservingStrategy networkObservingStrategy;
    @NonNull private final InternetObservingStrategy internetObservingStrategy;

    @VisibleForTesting(otherwise = PRIVATE)
    RxNetwork() {
        throw new AssertionError("Use static factory methods or Builder to initialize RxNetwork");
    }

    @VisibleForTesting(otherwise = PRIVATE)
    RxNetwork(@NonNull Builder builder) {
        checkNotNull(builder, "builder");
        this.scheduler = builder.scheduler;
        this.networkObservingStrategy = builder.networkObservingStrategy;
        this.internetObservingStrategy = builder.internetObservingStrategy;
    }

    @NonNull
    public static RxNetwork init(@NonNull Application application) {
        checkNotNull(application, "Cannot initialize RxNetwork with null application");
        return builder().init(application);
    }

    @NonNull
    public static Builder builder() { return new Builder(); }

    @Nullable
    public Scheduler scheduler() { return this.scheduler; }

    @NonNull
    public NetworkObservingStrategy networkObservingStrategy() {
        return this.networkObservingStrategy;
    }

    @NonNull
    public InternetObservingStrategy internetObservingStrategy() {
        return this.internetObservingStrategy;
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
    public Observable<RxNetworkInfo> observe() { return observe(networkObservingStrategy); }

    /**
     * RxNetworkInfo connectivity observable with all the original {@link NetworkInfo} information
     * that uses custom defined {@link NetworkObservingStrategy strategy}.
     *
     * @param strategy custom network observing strategy of type {@link NetworkObservingStrategy}
     *
     * @return {@link NetworkInfo} Observable
     */
    @NonNull
    public Observable<RxNetworkInfo> observe(@NonNull NetworkObservingStrategy strategy) {
        checkNotNull(strategy, "strategy");
        final Observable<RxNetworkInfo> observable = strategy.observe();
        if (scheduler != null) observable.subscribeOn(scheduler);
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
    public Observable<Boolean> observeSimple() { return observe().map(TO_CONNECTION_STATE); }

    /**
     * Real internet connectivity observable.
     *
     * @return {@code true} if there is real internet access, {@code false} otherwise
     */
    @NonNull
    public Observable<Boolean> observeReal() { return observeReal(internetObservingStrategy); }

    /**
     * Real internet connectivity observable with custom {@link InternetObservingStrategy}.
     *
     * @return {@code true} if there is real internet access, {@code false} otherwise
     */
    @NonNull
    public Observable<Boolean> observeReal(@NonNull InternetObservingStrategy strategy) {
        checkNotNull(strategy, "strategy");
        final Observable<Boolean> observable = strategy.observe();
        if (scheduler != null) observable.subscribeOn(scheduler);
        return observable;
    }

    public static final class Builder {

        private Scheduler scheduler;
        private NetworkObservingStrategy networkObservingStrategy;
        private InternetObservingStrategy internetObservingStrategy;

        public Builder defaultScheduler(@NonNull Scheduler scheduler) {
            this.scheduler = checkNotNull(scheduler, "scheduler");
            return this;
        }

        public Builder networkObservingStrategy(@NonNull NetworkObservingStrategy strategy) {
            networkObservingStrategy = checkNotNull(strategy, "strategy");
            return this;
        }

        public Builder networkObservingStrategyFactory(
                @NonNull NetworkObservingStrategyFactory factory) {

            checkNotNull(factory, "factory");
            networkObservingStrategy = factory.get();
            return this;
        }

        public Builder internetObservingStrategy(@NonNull InternetObservingStrategy strategy) {
            this.internetObservingStrategy = checkNotNull(strategy, "strategy");
            return this;
        }

        @NonNull
        public RxNetwork init(@NonNull Application application) {
            checkNotNull(application, "Cannot initialize RxNetwork with null application");

            if (networkObservingStrategy == null) {
                final Collection<NetworkObservingStrategyProvider> providers
                        = BuiltInNetworkObservingStrategyProviders.get(application);

                networkObservingStrategy = BuiltInStrategyFactory.create(providers).get();
            }

            if (internetObservingStrategy == null) {
                internetObservingStrategy = BuiltInInternetObservingStrategy.create();
            }

            return new RxNetwork(this);
        }
    }
}
