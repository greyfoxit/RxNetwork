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
package greyfox.rxnetwork.internal.strategy.network.providers;

import android.content.Context;
import greyfox.rxnetwork.BuildConfig;
import greyfox.rxnetwork.internal.strategy.ObservingStrategyProvider;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategyProvider;
import java.util.Collection;
import org.assertj.core.api.Condition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BuiltInNetworkObservingObservingStrategyProvidersTest {

  private final Context context = RuntimeEnvironment.application;

  private final Collection<NetworkObservingStrategyProvider> sut
      = new BuiltInNetworkObservingStrategyProviders(context).get();

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private NetworkObservingStrategyProvider anyProvider;

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateViaEmptyConstructor() {
    new BuiltInNetworkObservingStrategyProviders();
  }

  @Test
  public void shouldNeverBeNull() {
    assertThat(sut).isNotNull();
  }

  @Test
  public void shouldHaveAtLeastOneProvider() {
    assertThat(sut).hasAtLeastOneElementOfType(NetworkObservingStrategyProvider.class);
  }

  @Test
  @Config(sdk = KITKAT)
  public void shouldHaveOnlyOneProviderThatCanProvide_forGivenPlatform() {
    assertThat(sut).haveExactly(1, new CanProvide());
  }

  @Test
  public void shouldNotHaveDuplicateProviders() {
    assertThat(sut).doesNotHaveDuplicates();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldBeUnmodifiable() {
    // hacky but we don't want to test for every collection methods
    assertThat(sut.getClass().getName()).contains("Unmodifiable");
    sut.add(anyProvider);
  }

  private static final class CanProvide extends Condition<ObservingStrategyProvider> {

    @Override
    public boolean matches(ObservingStrategyProvider value) {
      return value.canProvide();
    }
  }
}
