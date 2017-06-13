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

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.IS_MOBILE;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.IS_WIFI;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.hasType;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Radek Kozak
 */
@RunWith(MockitoJUnitRunner.class)
public class RxNetworkInfoTypeTest {

  private static final Predicate<RxNetworkInfo> VALID_TYPES = hasType(TYPE_MOBILE, TYPE_WIFI);

  @Mock private RxNetworkInfo rxNetworkInfo;

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateViaConstructor() {
    new RxNetworkInfoPredicate.Type();
  }

  @Test
  public void shouldBeTrue_whenAtLeastOnePredicatedTypeOccurred() throws Exception {
    when(rxNetworkInfo.getType()).thenReturn(TYPE_WIFI);

    assertThat(VALID_TYPES.test(rxNetworkInfo)).isTrue();
  }

  @Test
  public void shouldBeFalse_whenNoneOfPredicatedTypesOccurred() throws Exception {
    when(rxNetworkInfo.getType()).thenReturn(TYPE_WIMAX);

    assertThat(VALID_TYPES.test(rxNetworkInfo)).isFalse();
  }

  @Test
  public void shouldBeOfTypeMobile() throws Exception {
    when(rxNetworkInfo.getType()).thenReturn(TYPE_MOBILE);

    assertThat(IS_MOBILE.test(rxNetworkInfo)).isTrue();
  }

  @Test
  public void shouldBeOfTypeWifi() throws Exception {
    when(rxNetworkInfo.getType()).thenReturn(TYPE_WIFI);

    assertThat(IS_WIFI.test(rxNetworkInfo)).isTrue();
  }
}
