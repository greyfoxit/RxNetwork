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

import android.net.NetworkCapabilities;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork.BuildConfig;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.functions.Predicate;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.hasCapability;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.hasTransportType;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.isSatisfiedByDownBandwidth;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.isSatisfiedByUpBandwidth;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Radek Kozak
 */
@RequiresApi(LOLLIPOP)
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP)
public class RxNetworkInfoCapabilitiesTest {

  private static final Predicate<RxNetworkInfo> VALID_NET_CAPABILITIES =
      hasCapability(NET_CAPABILITY_INTERNET, NET_CAPABILITY_NOT_RESTRICTED);

  private static final int INVALID_UP_BANDWIDTH = 2000;
  private static final int INVALID_DOWN_BANDWIDTH = 500;

  private static final Predicate<RxNetworkInfo> VALID_TRANSPORT_TYPES =
      hasTransportType(TRANSPORT_CELLULAR, TRANSPORT_WIFI);

  private static int VALID_UP_BANDWIDTH = 2048;
  private static final Predicate<RxNetworkInfo> VALID_UPSTREAM_PREDICATE =
      isSatisfiedByUpBandwidth(VALID_UP_BANDWIDTH);

  private static int VALID_DOWN_BANDWIDTH = 512;
  private static final Predicate<RxNetworkInfo> VALID_DOWNSTREAM_PREDICATE =
      isSatisfiedByDownBandwidth(VALID_DOWN_BANDWIDTH);

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private RxNetworkInfo rxNetworkInfo;
  @Mock private NetworkCapabilities networkCapabilities;

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateViaConstructor() {
    new RxNetworkInfoPredicate.Capabilities();
  }

  @Test
  public void shouldReturnTrue_whenAtLeastOnePredicatedTransportTypeOccurred() throws Exception {
    when(networkCapabilities.hasTransport(TRANSPORT_WIFI)).thenReturn(true);
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

    assertThat(VALID_TRANSPORT_TYPES.test(rxNetworkInfo)).isTrue();
  }

  @Test
  public void shouldReturnFalse_whenNoneOfPredicatedTransportTypesOccurred() throws Exception {
    when(networkCapabilities.hasTransport(TRANSPORT_WIFI)).thenReturn(false);
    when(networkCapabilities.hasTransport(TRANSPORT_CELLULAR)).thenReturn(false);
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

    assertThat(VALID_TRANSPORT_TYPES.test(rxNetworkInfo)).isFalse();
  }

  @Test
  public void shouldReturnTrue_whenAtLeastOnePredicatedCapabilityOccurred() throws Exception {
    when(networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET)).thenReturn(true);
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

    assertThat(VALID_NET_CAPABILITIES.test(rxNetworkInfo)).isTrue();
  }

  @Test
  public void shouldReturnFalse_whenNoneOfPredicatedCapabilitiesOccurred() throws Exception {
    when(networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET)).thenReturn(false);
    when(networkCapabilities.hasCapability(NET_CAPABILITY_NOT_RESTRICTED)).thenReturn(false);
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

    assertThat(VALID_NET_CAPABILITIES.test(rxNetworkInfo)).isFalse();
  }

  @Test
  public void shouldReturnFalse_whenNetworkInfoHasNoNetworkCapabilities() throws Exception {
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(null);

    assertThat(VALID_UPSTREAM_PREDICATE.test(rxNetworkInfo)).isFalse();
    assertThat(VALID_DOWNSTREAM_PREDICATE.test(rxNetworkInfo)).isFalse();
  }

  @Test
  public void shouldReturnTrue_whenUpBandwidthSatisfiedByNetwork() throws Exception {
    when(networkCapabilities.getLinkUpstreamBandwidthKbps()).thenReturn(VALID_UP_BANDWIDTH);
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

    assertThat(VALID_UPSTREAM_PREDICATE.test(rxNetworkInfo)).isTrue();
  }

  @Test
  public void shouldReturnFalse_whenUpBandwidthNotSatisfiedByNetwork() throws Exception {
    when(networkCapabilities.getLinkDownstreamBandwidthKbps()).thenReturn(INVALID_UP_BANDWIDTH);
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

    assertThat(VALID_UPSTREAM_PREDICATE.test(rxNetworkInfo)).isFalse();
  }

  @Test
  public void shouldReturnTrue_whenDownBandwidthSatisfiedByNetwork() throws Exception {
    when(networkCapabilities.getLinkDownstreamBandwidthKbps()).thenReturn(VALID_DOWN_BANDWIDTH);
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

    assertThat(VALID_DOWNSTREAM_PREDICATE.test(rxNetworkInfo)).isTrue();
  }

  @Test
  public void shouldReturnFalse_whenDownBandwidthNotSatisfiedByNetwork() throws Exception {
    when(networkCapabilities.getLinkDownstreamBandwidthKbps()).thenReturn(INVALID_DOWN_BANDWIDTH);
    when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

    assertThat(VALID_DOWNSTREAM_PREDICATE.test(rxNetworkInfo)).isFalse();
  }
}
