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
package greyfox.rxnetwork.internal.strategy.network.predicate;

import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.functions.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static android.net.NetworkInfo.State.CONNECTED;
import static android.net.NetworkInfo.State.CONNECTING;
import static android.net.NetworkInfo.State.DISCONNECTED;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.State.hasState;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Radek Kozak
 */
@RunWith(MockitoJUnitRunner.class)
public class RxNetworkInfoStateTest {

  private static final Predicate<RxNetworkInfo> VALID_STATES = hasState(CONNECTING, CONNECTED);

  @Mock private RxNetworkInfo rxNetworkInfo;

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateViaConstructor() {
    new RxNetworkInfoPredicate.State();
  }

  @Test
  public void shouldBeTrue_whenAtLeastOnePredicatedStateOccurred() throws Exception {
    when(rxNetworkInfo.getState()).thenReturn(CONNECTED);

    assertThat(VALID_STATES.test(rxNetworkInfo)).isTrue();
  }

  @Test
  public void shouldBeFalse_whenNoneOfPredicatedStatesOccurred() throws Exception {
    when(rxNetworkInfo.getState()).thenReturn(DISCONNECTED);

    assertThat(VALID_STATES.test(rxNetworkInfo)).isFalse();
  }
}
