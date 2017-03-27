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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.app.Application;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategyFactory;
import greyfox.rxnetwork2.internal.strategy.network.impl.LollipopNetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.impl.PreLollipopNetworkObservingStrategy;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.internal.schedulers.ImmediateThinScheduler;
import io.reactivex.schedulers.Schedulers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings("WeakerAccess")
@RunWith(MockitoJUnitRunner.class)
public class RxNetworkTest {

    Scheduler CUSTOM_SCHEDULER = Schedulers.trampoline();
    NetworkObservingStrategy NULL_STRATEGY = null;

    @Mock Application application;
    @Mock NetworkObservingStrategy CUSTOM_STRATEGY;
    @Mock NetworkObservingStrategyFactory CUSTOM_STRATEGY_FACTORY;
    @Mock Observable<RxNetworkInfo> VALID_OBSERVABLE;

    @Before
    public void setUp() throws Exception {
        RxNetwork.resetForTesting();
    }

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaConstructor() {
        new RxNetwork();
    }

    @Test
    public void shouldIgnoreCustomInitialization_whenAlreadyInitialized() {
        RxNetwork.init(application, CUSTOM_SCHEDULER);

        Scheduler CUSTOM_SCHEDULER = ImmediateThinScheduler.INSTANCE;
        RxNetwork.init(CUSTOM_STRATEGY_FACTORY, CUSTOM_SCHEDULER);

        assertThat(RxNetwork.scheduler()).isNotEqualTo(CUSTOM_SCHEDULER);
        assertThat(RxNetwork.strategy()).isInstanceOf(PreLollipopNetworkObservingStrategy.class);
    }

    @Test
    public void shouldInitWithDefaultStrategy_andNoScheduler() {
        RxNetwork.init(application);

        assertThat(RxNetwork.strategy()).isNotNull();
        assertThat(RxNetwork.scheduler()).isNull();
    }

    @Test
    public void shouldInitWithDefaultStrategy_andCustomScheduler() {
        RxNetwork.init(application, CUSTOM_SCHEDULER);

        assertThat(RxNetwork.strategy()).isNotNull();
        assertThat(RxNetwork.scheduler()).isNotNull().isEqualTo(CUSTOM_SCHEDULER);
    }

    @Test
    public void shouldInitWithCustomStrategyFactory_andNoScheduler() {
        when(CUSTOM_STRATEGY_FACTORY.get()).thenReturn(CUSTOM_STRATEGY);

        RxNetwork.init(CUSTOM_STRATEGY_FACTORY);

        assertThat(RxNetwork.strategy()).isNotNull().isEqualTo(CUSTOM_STRATEGY);
        assertThat(RxNetwork.scheduler()).isNull();
    }

    @Test
    public void shouldInitWithCustomStrategyFactory_andCustomScheduler() {
        when(CUSTOM_STRATEGY_FACTORY.get()).thenReturn(CUSTOM_STRATEGY);

        RxNetwork.init(CUSTOM_STRATEGY_FACTORY, CUSTOM_SCHEDULER);

        assertThat(RxNetwork.strategy()).isNotNull().isEqualTo(CUSTOM_STRATEGY);
        assertThat(RxNetwork.scheduler()).isNotNull().isEqualTo(CUSTOM_SCHEDULER);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToObserveWithoutProperInit() {
        RxNetwork.observe();
    }

    @Test
    public void observableShouldNeverBeNull_whenInitializedProperly() {
        RxNetwork.init(application, CUSTOM_SCHEDULER);

        assertThat(RxNetwork.observe()).isNotNull();
    }

    @Test
    public void observableShouldSubscribeCorrectly() {
        RxNetwork.init(application);

        RxNetwork.observe().test().assertSubscribed();
    }

    @Test(expected = NullPointerException.class)
    public void observeSimple_shouldThrow_whenTryingToObserveWithoutProperInit() {
        RxNetwork.observeSimple();
    }

    @Test
    public void observableSimpleShouldNeverBeNull_whenInitializedProperly() {
        RxNetwork.init(application, CUSTOM_SCHEDULER);

        assertThat(RxNetwork.observeSimple()).isNotNull();
    }

    @Test
    public void observableSimpleShouldSubscribeCorrectly() {
        RxNetwork.init(application);

        RxNetwork.observeSimple().test().assertSubscribed();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToObserveWithNullStrategy() {
        RxNetwork.observeWith(NULL_STRATEGY);
    }

    @Test
    public void observableWithStrategyShouldNeverBeNull_whenProvidedCustomStrategy() {
        when(CUSTOM_STRATEGY.observe()).thenReturn(VALID_OBSERVABLE);

        assertThat(RxNetwork.observeWith(CUSTOM_STRATEGY)).isNotNull();
    }

    @Test
    public void shouldSubscribeCorrectlyWithCustomStrategy() {
        NetworkObservingStrategy CUSTOM_STRATEGY
                = new LollipopNetworkObservingStrategy(application);

        RxNetwork.observeWith(CUSTOM_STRATEGY).test().assertSubscribed();
    }
}
