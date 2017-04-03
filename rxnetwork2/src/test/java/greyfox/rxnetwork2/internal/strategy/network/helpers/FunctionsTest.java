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
package greyfox.rxnetwork2.internal.strategy.network.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import static greyfox.rxnetwork2.internal.strategy.network.helpers.Functions.TO_CONNECTION_STATE;

import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import org.junit.Test;

/**
 * @author Radek Kozak
 */
public class FunctionsTest {

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaConstructor() {
        new Functions();
    }

    @Test
    public void shouldBeTrue_whenRxNetworkInfoIsConnected() throws Exception {
        RxNetworkInfo sut = RxNetworkInfo.builder().connected(true).build();
        boolean result = TO_CONNECTION_STATE.apply(sut);

        assertThat(result).isTrue();
    }

    @Test
    public void shouldBeFalse_whenRxNetworkInfoNotConnected() throws Exception {
        RxNetworkInfo sut = RxNetworkInfo.builder().connected(false).build();
        boolean result = TO_CONNECTION_STATE.apply(sut);

        assertThat(result).isFalse();
    }

    @Test
    public void shouldBeFalse_whenRxNetworkInfoIsNull() throws Exception {
        boolean result = TO_CONNECTION_STATE.apply(null);

        assertThat(result).isFalse();
    }
}
