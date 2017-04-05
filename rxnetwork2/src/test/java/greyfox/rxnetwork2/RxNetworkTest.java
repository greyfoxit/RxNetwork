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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Application;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.internet.impl.BuiltInInternetObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.internet.impl.SocketInternetObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategyFactory;
import greyfox.rxnetwork2.internal.strategy.network.impl.BuiltInNetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.impl.PreLollipopNetworkObservingStrategy;
import io.reactivex.Observable;
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

    @Mock Application application;
    @Mock NetworkObservingStrategy CUSTOM_NETWORK_STRATEGY;
    @Mock NetworkObservingStrategyFactory CUSTOM_NETWORK_STRATEGY_FACTORY;
    @Mock InternetObservingStrategy CUSTOM_INTERNET_STRATEGY;
    @Mock Observable<RxNetworkInfo> VALID_NETWORK_OBSERVABLE;
    @Mock Observable<Boolean> VALID_INTERNET_OBSERVABLE;

    @Before
    public void setUp() throws Exception {
        sut = RxNetwork.init(application);
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
    public void shouldThrow_whenTryingToInitializeWithNullApplication() {
        RxNetwork.builder().init(null);
    }

    @Test
    public void shouldInitWithDefaultStrategies_andNoScheduler() {
        assertThat((sut.networkObservingStrategy())).isNotNull()
                .isInstanceOf(BuiltInNetworkObservingStrategy.class);
        assertThat((sut.internetObservingStrategy())).isNotNull()
                .isInstanceOf(BuiltInInternetObservingStrategy.class);
        assertThat(sut.scheduler()).isNull();
    }

    @Test
    public void shouldInitWithCustomScheduler() {
        sut = RxNetwork.builder().defaultScheduler(CUSTOM_SCHEDULER).init(application);

        assertThat(sut.scheduler()).isNotNull().isEqualTo(CUSTOM_SCHEDULER);
    }

    @Test
    public void shouldInitWithCustomNetworkObservingStrategy_viaFactory() {
        when(CUSTOM_NETWORK_STRATEGY_FACTORY.get()).thenReturn(CUSTOM_NETWORK_STRATEGY);

        sut = RxNetwork.builder().networkObservingStrategyFactory(CUSTOM_NETWORK_STRATEGY_FACTORY)
                .init(application);

        assertThat(sut.networkObservingStrategy()).isNotNull().isEqualTo(CUSTOM_NETWORK_STRATEGY);
    }

    @Test
    public void shouldInitWithCustomNetworkObservingStrategy() {
        sut = RxNetwork.builder().networkObservingStrategy(CUSTOM_NETWORK_STRATEGY)
                .init(application);

        assertThat(sut.networkObservingStrategy()).isNotNull().isEqualTo(CUSTOM_NETWORK_STRATEGY);
    }

    @Test
    public void shouldInitWithCustomInternetObservingStrategy() {
        sut = RxNetwork.builder().internetObservingStrategy(CUSTOM_INTERNET_STRATEGY)
                .init(application);

        assertThat(sut.internetObservingStrategy()).isNotNull().isEqualTo(CUSTOM_INTERNET_STRATEGY);
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
        doReturn(VALID_NETWORK_OBSERVABLE).when(CUSTOM_NETWORK_STRATEGY).observe();

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
        InternetObservingStrategy builtInStrategy = spy(sut.internetObservingStrategy());
        doReturn(VALID_INTERNET_OBSERVABLE).when(CUSTOM_INTERNET_STRATEGY).observe();

        sut.observeReal(CUSTOM_INTERNET_STRATEGY);

        verify(CUSTOM_INTERNET_STRATEGY).observe();
        verify(builtInStrategy, never()).observe();
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
        CUSTOM_NETWORK_STRATEGY = new PreLollipopNetworkObservingStrategy(application);

        sut = RxNetwork.builder().networkObservingStrategy(CUSTOM_NETWORK_STRATEGY)
                .init(application);

        sut.observe(CUSTOM_NETWORK_STRATEGY).test().assertSubscribed();
    }

    @Test
    public void shouldSubscribeCorrectly_withCustomInternetObservingStrategy() {
        CUSTOM_INTERNET_STRATEGY = SocketInternetObservingStrategy.create();

        sut = RxNetwork.builder().internetObservingStrategy(CUSTOM_INTERNET_STRATEGY)
                .init(application);

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
