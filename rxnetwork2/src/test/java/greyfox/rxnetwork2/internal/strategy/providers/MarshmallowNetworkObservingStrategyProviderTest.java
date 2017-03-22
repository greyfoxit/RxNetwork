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
package greyfox.rxnetwork2.internal.strategy.providers;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;

import static org.assertj.core.api.Assertions.assertThat;

import greyfox.rxnetwork2.BuildConfig;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.impl.MarshmallowNetworkObservingStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * @author Radek Kozak
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class MarshmallowNetworkObservingStrategyProviderTest {

    MarshmallowNetworkObservingStrategyProvider sut
            = new MarshmallowNetworkObservingStrategyProvider(RuntimeEnvironment.application);

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenProvidedNullContext() {
        new MarshmallowNetworkObservingStrategyProvider(null);
    }

    @Test
    @Config(minSdk = M)
    public void shouldProvide_whenAtLeastMarshmallow() throws Exception {
        assertThat(sut.canProvide()).isTrue();
    }

    @Test
    @Config(maxSdk = LOLLIPOP_MR1)
    public void shouldNotProvide_whenOnPreMarshmallow() throws Exception {
        assertThat(sut.canProvide()).isFalse();
    }

    @Test
    public void shouldProvideConcreteStrategy() throws Exception {
        assertThat(sut.provide()).isNotNull().isInstanceOf(NetworkObservingStrategy.class)
                .isExactlyInstanceOf(MarshmallowNetworkObservingStrategy.class);
    }
}
