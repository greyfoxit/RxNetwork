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
package greyfox.rxnetwork.internal.strategy.network.factory;

import android.content.Context;
import greyfox.rxnetwork.internal.strategy.ObservingStrategyFactory;
import greyfox.rxnetwork.internal.strategy.network.providers.BuiltInNetworkObservingStrategyProviders;
import greyfox.rxnetwork.internal.strategy.network.providers.NetworkObservingStrategyProvider;
import greyfox.rxnetwork.internal.strategy.network.providers.ObservingStrategyProviders;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@SuppressWarnings("ConstantConditions")
@RunWith(MockitoJUnitRunner.class)
public class BuiltInNetworkObservingStrategyFactoryTest {

  private static final Collection<NetworkObservingStrategyProvider> EMPTY_PROVIDERS =
      Collections.emptySet();

  private ObservingStrategyProviders<NetworkObservingStrategyProvider> providers;
  private ObservingStrategyFactory sut;

  @Mock private Context context;

  @Before
  public void setUp() {
    providers = spy(new BuiltInNetworkObservingStrategyProviders(context));
    sut = BuiltInNetworkObservingStrategyFactory.create(providers);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingInstantiateWithNullProviders_viaConstructor() {
    new BuiltInNetworkObservingStrategyFactory(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToCreateWithNullProviders() {
    BuiltInNetworkObservingStrategyFactory.create(null);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenProvidersEmpty() {
    doReturn(EMPTY_PROVIDERS).when(providers).get();
    sut = BuiltInNetworkObservingStrategyFactory.create(providers);

    sut.get();
  }

  @Test
  public void shouldNeverGetNullStrategy() {
    assertThat(sut.get()).isNotNull();
  }
}
