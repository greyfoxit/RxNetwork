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

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
@RunWith(MockitoJUnitRunner.class)
public class RxNetworkTest {

  private static final Scheduler CUSTOM_SCHEDULER = Schedulers.trampoline();
  private RxNetwork sut;

  @Mock private Context context;
  @Mock private NetworkObservingStrategy customNetworkStrategy;
  @Mock private NetworkObservingStrategyFactory customNetworkStrategyFactory;
  @Mock private InternetObservingStrategy customInternetStrategy;
  @Mock private InternetObservingStrategyFactory customInternetStrategyFactory;
  @Mock private NetworkRequest customNetworkRequest;

  @Before
  public void setUp() {
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
    assertThat(sut.internetObservingStrategy()).isNotNull();
  }

  @Test
  public void shouldDefaultToWalledGardenInternetObservingStrategy_whenInitializedWithoutContext() {

    sut = RxNetwork.init();

    assertThat((sut.networkObservingStrategy())).isNull();
    assertThat(sut.internetObservingStrategy()).isInstanceOf(WalledGardenInternetObservingStrategy.class);
  }

  @Test
  public void shouldInitWithNonNullStrategies_whenInitializedWithContext() {
    assertThat(sut.internetObservingStrategy()).isNotNull();
    assertThat(sut.networkObservingStrategy()).isNotNull();
  }

  @Test
  public void shouldInitWithNoScheduler() {
    assertThat(sut.scheduler()).isNull();
  }

  @Test
  public void shouldInitWithCustomScheduler() {
    sut = RxNetwork.builder().defaultScheduler(CUSTOM_SCHEDULER).init(context);

    assertThat(sut.scheduler()).isNotNull().isEqualTo(CUSTOM_SCHEDULER);
  }

  @Test
  public void shouldInitWithCustomNetworkObservingStrategy() {
    sut = RxNetwork.builder().networkObservingStrategy(customNetworkStrategy).init(context);

    assertThat(sut.networkObservingStrategy()).isNotNull().isEqualTo(customNetworkStrategy);
  }

  @Test
  public void shouldInitWithCustomNetworkObservingStrategy_viaFactory() {
    when(customNetworkStrategyFactory.get()).thenReturn(customNetworkStrategy);

    sut = RxNetwork.builder().networkObservingStrategyFactory(customNetworkStrategyFactory)
        .init(context);

    assertThat(sut.networkObservingStrategy()).isNotNull().isEqualTo(customNetworkStrategy);
  }

  @Test
  public void shouldInitWithCustomInternetObservingStrategy() {
    sut = RxNetwork.builder().internetObservingStrategy(customInternetStrategy).init(context);

    assertThat(sut.internetObservingStrategy()).isNotNull().isEqualTo(customInternetStrategy);
  }

  @Test
  public void shouldInitWithCustomInternetObservingStrategy_viaFactory() {
    when(customInternetStrategyFactory.get()).thenReturn(customInternetStrategy);

    sut = RxNetwork.builder().internetObservingStrategyFactory(customInternetStrategyFactory)
        .init(context);

    assertThat(sut.internetObservingStrategy()).isNotNull().isEqualTo(customInternetStrategy);
  }

  @Test
  public void shouldInitWithCustomNetworkRequest() {
    sut = RxNetwork.builder().defaultNetworkRequest(customNetworkRequest).init(context);

    assertThat(sut.networkRequest()).isNotNull().isEqualTo(customNetworkRequest);
  }

  @Test
  public void shouldNeverBeNull() {
    assertThat(sut.observe()).isNotNull();
    assertThat(sut.observeSimple()).isNotNull();
    assertThat(sut.observeReal()).isNotNull();
  }

  @Test
  public void shouldSubscribeCorrectly_toObserve() {
    sut.observe().test().assertSubscribed();
  }

  @Test
  public void shouldOverrideDefaultStrategyWithCustom_whenProvidedToObserve() {
    NetworkObservingStrategy builtInStrategy = spy(sut.networkObservingStrategy());

    sut.observe(customNetworkStrategy);

    verify(customNetworkStrategy).observe();
    verify(builtInStrategy, never()).observe();
  }

  @Test
  public void shouldSubscribeCorrectly_toObserveSimple() {
    sut.observeSimple().test().assertSubscribed();
  }

  @Test
  public void shouldSubscribeCorrectly_toObserveReal() {
    sut.observeReal().test().assertSubscribed();
  }

  @Test
  public void shouldOverrideDefaultStrategyWithCustom_whenProvidedToObserveReal() {
    sut = spy(sut);

    sut.observeReal(customInternetStrategy);

    verify(customInternetStrategy).observe();
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
    customNetworkStrategy = new PreLollipopNetworkObservingStrategy(context);

    sut = RxNetwork.builder().networkObservingStrategy(customNetworkStrategy).init(context);

    sut.observe(customNetworkStrategy).test().assertSubscribed();
  }

  @Test
  public void shouldSubscribeCorrectly_withCustomNetworkObservingStrategy_andScheduler() {

    customNetworkStrategy = new PreLollipopNetworkObservingStrategy(context);

    sut = RxNetwork.builder().networkObservingStrategy(customNetworkStrategy)
        .defaultScheduler(CUSTOM_SCHEDULER).init(context);

    sut.observe(customNetworkStrategy).test().assertSubscribed();
  }

  @Test
  public void shouldSubscribeCorrectly_withCustomInternetObservingStrategy() {
    customInternetStrategy = SocketInternetObservingStrategy.create();

    sut = RxNetwork.builder().internetObservingStrategy(customInternetStrategy).init(context);

    sut.observeReal(customInternetStrategy).test().assertSubscribed();
  }

  @Test
  public void shouldSubscribeCorrectly_withCustomInternetObservingStrategy_andScheduler() {

    customInternetStrategy = SocketInternetObservingStrategy.create();

    sut = RxNetwork.builder().internetObservingStrategy(customInternetStrategy)
        .defaultScheduler(CUSTOM_SCHEDULER).init(context);

    sut.observeReal(customInternetStrategy).test().assertSubscribed();
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToSetNullInternetStrategyOnBuilder() {
    RxNetwork.builder().internetObservingStrategy(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToSetNullNetworkStrategyOnBuilder() {
    RxNetwork.builder().networkObservingStrategy(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToSetNullSchedulerOnBuilder() {
    RxNetwork.builder().defaultScheduler(null);
  }
}
