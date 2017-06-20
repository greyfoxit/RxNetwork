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
import greyfox.rxnetwork.internal.strategy.network.impl.LollipopNetworkObservingStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class LollipopNetworkObservingObservingStrategyProviderTest {

  private final LollipopNetworkObservingStrategyProvider sut =
      new LollipopNetworkObservingStrategyProvider(RuntimeEnvironment.application);

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenTryingToInitializeWithNullContext() {
    new LollipopNetworkObservingStrategyProvider(null);
  }

  @Test
  @Config(sdk = { LOLLIPOP, LOLLIPOP_MR1 })
  public void shouldProvide_whenAtLeastLollipop_andLowerThanMarshmallow() {
    assertThat(sut.canProvide()).isTrue();
  }

  @Test
  @Config(sdk = KITKAT)
  public void shouldNotProvide_whenOnPreLollipop() {
    assertThat(sut.canProvide()).isFalse();
  }

  @Test
  @Config(sdk = M)
  public void shouldNotProvide_whenOnMarshmallowOrHigher() {
    assertThat(sut.canProvide()).isFalse();
  }

  @Test
  @Config(sdk = LOLLIPOP)
  public void shouldProvideConcreteStrategy() {
    assertThat(sut.provide()).isNotNull()
        .isExactlyInstanceOf(LollipopNetworkObservingStrategy.class);
  }
}
