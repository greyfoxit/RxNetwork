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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.NetworkRequest;
import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.internet.InternetObservingStrategyFactory;
import greyfox.rxnetwork.internal.strategy.internet.impl.SocketInternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.internet.impl.WalledGardenInternetObservingStrategy;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategyFactory;
import greyfox.rxnetwork.internal.strategy.network.impl.PreLollipopNetworkObservingStrategy;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings({"WeakerAccess", "ConstantConditions"})
@RunWith(MockitoJUnitRunner.class)
public class RxNetworkTest {

    Scheduler CUSTOM_SCHEDULER = Schedulers.trampoline();
    RxNetwork sut;

    @Mock Context context;
    @Mock NetworkObservingStrategy CUSTOM_NETWORK_STRATEGY;
    @Mock NetworkObservingStrategyFactory CUSTOM_NETWORK_STRATEGY_FACTORY;
    @Mock InternetObservingStrategy CUSTOM_INTERNET_STRATEGY;
    @Mock InternetObservingStrategy BUILTIN_INTERNET_STRATEGY;
    @Mock InternetObservingStrategyFactory CUSTOM_INTERNET_STRATEGY_FACTORY;
    @Mock NetworkRequest CUSTOM_NETWORK_REQUEST;

    @Before
    public void setUp() throws Exception {
        doReturn(context).when(context).getApplicationContext();
        sut = RxNetwork.init(context);
    }

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaConstructor() {
        new RxNetwork();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithNullBuilder() {
        new RxNetwork(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInitializeWithNullContext() {
        RxNetwork.builder().init(null);
    }

    @Test
    public void shouldInitWithInternetObservingStrategyOnly_whenInitializedWithoutContext() {
        sut = RxNetwork.init();

        assertThat((sut.networkObservingStrategy())).isNull();
        assertThat(sut.internetObservingStrategy()).isNotNull()
                .isInstanceOf(WalledGardenInternetObservingStrategy.class);
    }

    @Test
    public void shouldInitWithDefaultStrategies_andNoScheduler() {
        assertThat((sut.internetObservingStrategy())).isNotNull()
                .isInstanceOf(WalledGardenInternetObservingStrategy.class);
        assertThat(sut.scheduler()).isNull();
    }

    @Test
    public void shouldInitWithCustomScheduler() {
        sut = RxNetwork.builder().defaultScheduler(CUSTOM_SCHEDULER).init(context);

        assertThat(sut.scheduler()).isNotNull().isEqualTo(CUSTOM_SCHEDULER);
    }

    @Test
    public void shouldInitWithCustomNetworkObservingStrategy_viaFactory() {
        when(CUSTOM_NETWORK_STRATEGY_FACTORY.get()).thenReturn(CUSTOM_NETWORK_STRATEGY);

        sut = RxNetwork.builder().networkObservingStrategyFactory(CUSTOM_NETWORK_STRATEGY_FACTORY)
                .init(context);

        assertThat(sut.networkObservingStrategy()).isNotNull().isEqualTo(CUSTOM_NETWORK_STRATEGY);
    }

    @Test
    public void shouldInitWithCustomNetworkObservingStrategy() {
        sut = RxNetwork.builder().networkObservingStrategy(CUSTOM_NETWORK_STRATEGY)
                .init(context);

        assertThat(sut.networkObservingStrategy()).isNotNull().isEqualTo(CUSTOM_NETWORK_STRATEGY);
    }

    @Test
    public void shouldInitWithCustomInternetObservingStrategy_viaFactory() {
        when(CUSTOM_INTERNET_STRATEGY_FACTORY.get()).thenReturn(CUSTOM_INTERNET_STRATEGY);

        sut = RxNetwork.builder().internetObservingStrategyFactory(CUSTOM_INTERNET_STRATEGY_FACTORY)
                .init(context);

        assertThat(sut.internetObservingStrategy()).isNotNull().isEqualTo(CUSTOM_INTERNET_STRATEGY);
    }

    @Test
    public void shouldInitWithCustomInternetObservingStrategy() {
        sut = RxNetwork.builder().internetObservingStrategy(CUSTOM_INTERNET_STRATEGY)
                .init(context);

        assertThat(sut.internetObservingStrategy()).isNotNull().isEqualTo(CUSTOM_INTERNET_STRATEGY);
    }

    @Test
    public void shouldInitWithCustomNetworkRequest() {
        sut = RxNetwork.builder().defaultNetworkRequest(CUSTOM_NETWORK_REQUEST)
                .init(context);

        assertThat(sut.networkRequest()).isNotNull().isEqualTo(CUSTOM_NETWORK_REQUEST);
    }

    @Test
    public void observableShouldNeverBeNull() {
        assertThat(sut.observe()).isNotNull();
    }

    @Test
    public void observableShouldSubscribeCorrectly() {
        sut.observe().test().assertSubscribed();
    }

    @Test
    public void observableShouldUseCustomStrategyInsteadOfDefault() {
        NetworkObservingStrategy builtInStrategy = spy(sut.networkObservingStrategy());

        sut.observe(CUSTOM_NETWORK_STRATEGY);

        verify(CUSTOM_NETWORK_STRATEGY).observe();
        verify(builtInStrategy, never()).observe();
    }

    @Test
    public void observableSimpleShouldNeverBeNull() {
        assertThat(sut.observeSimple()).isNotNull();
    }

    @Test
    public void observableSimpleShouldSubscribeCorrectly() {
        sut.observeSimple().test().assertSubscribed();
    }

    @Test
    public void observableRealShouldNeverBeNull() {
        assertThat(sut.observeReal()).isNotNull();
    }

    @Test
    public void observableRealShouldSubscribeCorrectly() {
        sut.observeReal().test().assertSubscribed();
    }

    @Test
    public void observableReal_shouldUseCustomStrategyInsteadOfDefault() {
        sut = spy(sut);

        sut.observeReal(CUSTOM_INTERNET_STRATEGY);

        verify(CUSTOM_INTERNET_STRATEGY).observe();
        verify(sut, never()).internetObservingStrategy();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToObserveWithNullNetworkStrategy() {
        sut.observe(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToObserveWithNullInternetStrategy() {
        sut.observeReal(null);
    }

    @Test
    public void shouldSubscribeCorrectly_withCustomNetworkObservingStrategy() {
        CUSTOM_NETWORK_STRATEGY = new PreLollipopNetworkObservingStrategy(context);

        sut = RxNetwork.builder().networkObservingStrategy(CUSTOM_NETWORK_STRATEGY)
                .init(context);

        sut.observe(CUSTOM_NETWORK_STRATEGY).test().assertSubscribed();
    }

    @Test
    public void shouldSubscribeCorrectly_withCustomNetworkObservingStrategy_andScheduler() {
        CUSTOM_NETWORK_STRATEGY = new PreLollipopNetworkObservingStrategy(context);

        sut = RxNetwork.builder().networkObservingStrategy(CUSTOM_NETWORK_STRATEGY)
                .defaultScheduler(CUSTOM_SCHEDULER).init(context);

        sut.observe(CUSTOM_NETWORK_STRATEGY).test().assertSubscribed();
    }

    @Test
    public void shouldSubscribeCorrectly_withCustomInternetObservingStrategy() {
        CUSTOM_INTERNET_STRATEGY = SocketInternetObservingStrategy.create();

        sut = RxNetwork.builder().internetObservingStrategy(CUSTOM_INTERNET_STRATEGY)
                .init(context);

        sut.observeReal(CUSTOM_INTERNET_STRATEGY).test().assertSubscribed();
    }

    @Test
    public void shouldSubscribeCorrectly_withCustomInternetObservingStrategy_andScheduler() {
        CUSTOM_INTERNET_STRATEGY = SocketInternetObservingStrategy.create();

        sut = RxNetwork.builder().internetObservingStrategy(CUSTOM_INTERNET_STRATEGY)
                .defaultScheduler(CUSTOM_SCHEDULER).init(context);

        sut.observeReal(CUSTOM_INTERNET_STRATEGY).test().assertSubscribed();
    }

    @Test(expected = NullPointerException.class)
    public void builderShouldThrow_whenTryingToSetNullInternetStrategy() {
        RxNetwork.builder().internetObservingStrategy(null);
    }

    @Test(expected = NullPointerException.class)
    public void builderShouldThrow_whenTryingToSetNullNetworkStrategy() {
        RxNetwork.builder().networkObservingStrategy(null);
    }

    @Test(expected = NullPointerException.class)
    public void builderShouldThrow_whenTryingToSetNullScheduler() {
        RxNetwork.builder().defaultScheduler(null);
    }
}
