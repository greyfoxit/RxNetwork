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
package greyfox.rxnetwork2.internal.strategy.network.factory;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.content.Context;
import greyfox.rxnetwork2.internal.strategy.ObservingStrategyFactory;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategyProvider;
import greyfox.rxnetwork2.internal.strategy.network.providers.BuiltInNetworkObservingStrategyProviders;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Radek Kozak
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
@RunWith(MockitoJUnitRunner.class)
public class BuiltInNetworkObservingStrategyFactoryTest {

    static Collection<NetworkObservingStrategyProvider> EMPTY_PROVIDERS = Collections.emptySet();

    @Mock Context context;

    ObservingStrategyFactory sut;

    @Before
    public void setUp() {
        Collection<NetworkObservingStrategyProvider> providers
                = new BuiltInNetworkObservingStrategyProviders(context).get();
        sut = BuiltInNetworkObservingStrategyFactory.create(providers);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingInstantiateWithNullProviders() {
        new BuiltInNetworkObservingStrategyFactory(null);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToCreateWithNullProviders() {
        BuiltInNetworkObservingStrategyFactory.create(null);
    }

    @Test
    public void shouldNeverGetNullStrategy() throws Exception {
        assertThat(sut.get()).isNotNull();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenProvidersEmpty() throws Exception {
        sut = BuiltInNetworkObservingStrategyFactory.create(EMPTY_PROVIDERS);

        sut.get();
    }
}
