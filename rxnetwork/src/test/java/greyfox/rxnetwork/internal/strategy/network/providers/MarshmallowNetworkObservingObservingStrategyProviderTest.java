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

import greyfox.rxnetwork.BuildConfig;
import greyfox.rxnetwork.internal.strategy.network.impl.MarshmallowNetworkObservingStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Java6Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MarshmallowNetworkObservingObservingStrategyProviderTest {

  private final MarshmallowNetworkObservingStrategyProvider sut =
      new MarshmallowNetworkObservingStrategyProvider(RuntimeEnvironment.application);

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToInitializeWithNullContext() {
    new MarshmallowNetworkObservingStrategyProvider(null);
  }

  @Test
  @Config(sdk = M)
  public void shouldProvide_whenAtLeastMarshmallow() {
    assertThat(sut.canProvide()).isTrue();
  }

  @Test
  @Config(sdk = LOLLIPOP)
  public void shouldNotProvide_whenOnPreMarshmallow() {
    assertThat(sut.canProvide()).isFalse();
  }

  @Test
  @Config(sdk = M)
  public void shouldProvideConcreteStrategy() {
    assertThat(sut.provide()).isNotNull()
        .isExactlyInstanceOf(MarshmallowNetworkObservingStrategy.class);
  }
}
