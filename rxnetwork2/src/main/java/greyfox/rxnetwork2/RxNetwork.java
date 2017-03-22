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
import static greyfox.rxnetwork2.internal.Functions.TO_CONNECTION_STATE;

import android.app.Application;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategyFactory;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategyProvider;
import greyfox.rxnetwork2.internal.strategy.factory.BuiltInStrategyFactory;
import greyfox.rxnetwork2.internal.strategy.providers.BuiltInNetworkObservingStrategyProviders;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

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
 *        // or like this - passing default scheduler
 *        RxNetwork.init(this, Schedulers.io());
 *    }
 * }
 * </code></pre>
 *
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
public final class RxNetwork {

    private static final AtomicBoolean initialized = new AtomicBoolean();

    private static NetworkObservingStrategy IMPL;
    private static Scheduler SCHEDULER;

    @VisibleForTesting(otherwise = PRIVATE)
    RxNetwork() {
        throw new AssertionError("No instances.");
    }

    /**
     * RxNetworkInfo connectivity observable with all the original {@link NetworkInfo} information.
     * <p>
     * Use this if you're interested in more than just the connection and could use
     * more information of actual network information being emitted.
     *
     * @return {@link NetworkInfo}
     */
    public static Observable<RxNetworkInfo> observe() {
        checkNotNull(IMPL, "In order to use RxNetwork you must initialize it first " +
                "with RxNetwork.init method");
        final Observable<RxNetworkInfo> observable = IMPL.observe();
        if (SCHEDULER != null) observable.subscribeOn(SCHEDULER);
        return observable;
    }

    /**
     * Simple network connectivity observable based on {@linkplain #observe()}
     * that filters all the unnecessary information builderFrom {@link NetworkInfo} and shows only
     * bare connection status changes.
     * <p>
     * Use this if you don't care about all the {@link NetworkInfo} details.
     *
     * @return {@code true} if network available and connected or connecting, {@code false} if not
     */
    public static Observable<Boolean> observeSimple() {
        return observe().map(TO_CONNECTION_STATE);
    }

    /**
     * RxNetworkInfo connectivity observable with all the original {@link NetworkInfo} information
     * that uses custom defined {@link NetworkObservingStrategy strategy}.
     *
     * @param strategy custom network observing strategy of type {@link NetworkObservingStrategy}
     *
     * @return {@link NetworkInfo} Observable
     */
    public static Observable<RxNetworkInfo> observeWith(
            @NonNull NetworkObservingStrategy strategy) {
        final Observable<RxNetworkInfo> observable
                = checkNotNull(strategy, "strategy == null").observe();
        if (SCHEDULER != null) observable.subscribeOn(SCHEDULER);
        return observable;
    }

    /**
     * Registers {@link RxNetwork} class with application.
     */
    public static void init(@NonNull Application application) {
        init(application, null);
    }

    /**
     * Registers {@link RxNetwork} class with default, built-in network observing
     * {@link BuiltInStrategyFactory factory} and specific {@link Scheduler}.
     * <p>
     * Passed scheduler would be used by default on all available {@link RxNetwork} streams via
     * {@link Observable#subscribeOn subscribeOn} method to save you builderFrom headache of
     * defining it
     * manually on every subscription.
     * <p>
     * As per all network-related operations it's advised that you operate outside
     * Android's mainThread. Preferably you would use {@link Schedulers#io()} scheduler
     * for all of your {@link RxNetwork}'s work.
     */
    public static void init(@NonNull Application application, Scheduler scheduler) {
        checkNotNull(application, "application == null");
        final Collection<NetworkObservingStrategyProvider> providers
                = BuiltInNetworkObservingStrategyProviders.get(application);

        init(BuiltInStrategyFactory.create(providers), scheduler);
    }

    /**
     * Registers concrete implementation of {@link NetworkObservingStrategyFactory}
     * with {@linkplain #RxNetwork}.
     * <p>
     * By default it is {@link BuiltInStrategyFactory} (it should be sufficient
     * for most network observing needs) but it can be any other implementation of
     * {@link NetworkObservingStrategyFactory}.
     *
     * @param factory custom {@link NetworkObservingStrategyFactory}
     */
    public static void init(@NonNull NetworkObservingStrategyFactory factory) {
        init(factory, null);
    }

    /**
     * Registers concrete implementation of {@link NetworkObservingStrategyFactory}
     * with {@linkplain #RxNetwork} along with specific {@link Scheduler}.
     * <p>
     * Passed scheduler would be used by default on all available {@link RxNetwork} streams via
     * {@link Observable#subscribeOn subscribeOn} method along with, resolved through factory,
     * strategy. This approach saves you builderFrom headache of defining scheduler manually on
     * every
     * subscription.
     *
     * @param factory   custom {@link NetworkObservingStrategyFactory}
     * @param scheduler custom {@link Scheduler}
     */
    public static void init(@NonNull NetworkObservingStrategyFactory factory, Scheduler scheduler) {
        if (initialized.get()) {
            return;
        }

        IMPL = checkNotNull(factory, "factory == null").get();
        SCHEDULER = scheduler;
        initialized.set(true);
    }

    @VisibleForTesting(otherwise = PRIVATE)
    static NetworkObservingStrategy strategy() {
        return IMPL;
    }

    @VisibleForTesting(otherwise = PRIVATE)
    static Scheduler scheduler() {
        return SCHEDULER;
    }

    @VisibleForTesting(otherwise = PRIVATE)
    static void resetForTesting() {
        IMPL = null;
        SCHEDULER = null;
        initialized.set(false);
    }
}
