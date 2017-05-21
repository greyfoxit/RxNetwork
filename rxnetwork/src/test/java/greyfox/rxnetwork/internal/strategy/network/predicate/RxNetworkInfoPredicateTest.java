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

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_WIMAX;
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.net.NetworkInfo.State.CONNECTED;
import static android.net.NetworkInfo.State.CONNECTING;
import static android.net.NetworkInfo.State.DISCONNECTED;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.hasCapability;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.hasTransport;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.isSatisfiedByDownBandwidth;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Capabilities.isSatisfiedByUpBandwidth;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.State.hasState;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.IS_MOBILE;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.IS_WIFI;
import static greyfox.rxnetwork.internal.strategy.network.predicate.RxNetworkInfoPredicate.Type.hasType;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

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

@RequiresApi(api = android.os.Build.VERSION_CODES.LOLLIPOP)
@SuppressWarnings("WeakerAccess")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP)
public class RxNetworkInfoPredicateTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    Predicate<RxNetworkInfo> VALID_STATES = hasState(CONNECTING, CONNECTED);
    Predicate<RxNetworkInfo> VALID_TYPES = hasType(TYPE_MOBILE, TYPE_WIFI);
    Predicate<RxNetworkInfo> VALID_TRANSPORT_TYPES
            = hasTransport(TRANSPORT_CELLULAR, TRANSPORT_WIFI);
    Predicate<RxNetworkInfo> VALID_NET_CAPABILITIES
            = hasCapability(NET_CAPABILITY_INTERNET, NET_CAPABILITY_NOT_RESTRICTED);

    int VALID_UP_BANDWIDTH = 2048;
    int VALID_DOWN_BANDWIDTH = 512;

    Predicate<RxNetworkInfo> VALID_UPSTREAM_PREDICATE = isSatisfiedByUpBandwidth(VALID_UP_BANDWIDTH);
    Predicate<RxNetworkInfo> VALID_DOWNSTREAM_PREDICATE = isSatisfiedByDownBandwidth(VALID_DOWN_BANDWIDTH);

    int INVALID_UP_BANDWIDTH = 2000;
    int INVALID_DOWN_BANDWIDTH = 500;

    @Mock RxNetworkInfo rxNetworkInfo;
    @Mock NetworkCapabilities networkCapabilities;

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiate() {
        new RxNetworkInfoPredicate();
    }

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateState() {
        new RxNetworkInfoPredicate.State();
    }

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateType() {
        new RxNetworkInfoPredicate.Type();
    }

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateCapabilities() {
        new RxNetworkInfoPredicate.Capabilities();
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

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test
    public void shouldBeTrue_whenAtLeastOnePredicatedTransportTypeOccurred() throws Exception {
        when(networkCapabilities.hasTransport(TRANSPORT_WIFI)).thenReturn(true);
        when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

        assertThat(VALID_TRANSPORT_TYPES.test(rxNetworkInfo)).isTrue();
    }

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test
    public void shouldBeFalse_whenNoneOfPredicatedTransportTypesOccurred() throws Exception {
        when(networkCapabilities.hasTransport(TRANSPORT_WIFI)).thenReturn(false);
        when(networkCapabilities.hasTransport(TRANSPORT_CELLULAR)).thenReturn(false);
        when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

        assertThat(VALID_TRANSPORT_TYPES.test(rxNetworkInfo)).isFalse();
    }

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test
    public void shouldBeTrue_whenAtLeastOnePredicatedCapabilityOccurred() throws Exception {
        when(networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET)).thenReturn(true);
        when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

        assertThat(VALID_NET_CAPABILITIES.test(rxNetworkInfo)).isTrue();
    }

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test
    public void shouldBeFalse_whenNoneOfPredicatedCapabilitiesOccurred() throws Exception {
        when(networkCapabilities.hasCapability(NET_CAPABILITY_INTERNET)).thenReturn(false);
        when(networkCapabilities.hasCapability(NET_CAPABILITY_NOT_RESTRICTED)).thenReturn(false);
        when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

        assertThat(VALID_NET_CAPABILITIES.test(rxNetworkInfo)).isFalse();
    }

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test
    public void shouldBeTrue_whenUpBandwidthSatisfied() throws Exception {
        when(networkCapabilities.getLinkUpstreamBandwidthKbps()).thenReturn(VALID_UP_BANDWIDTH);
        when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

        assertThat(VALID_UPSTREAM_PREDICATE.test(rxNetworkInfo)).isTrue();
    }

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test
    public void shouldBeFalse_whenUpBandwidthNotSatisfied() throws Exception {
        when(networkCapabilities.getLinkDownstreamBandwidthKbps()).thenReturn(INVALID_UP_BANDWIDTH);
        when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

        assertThat(VALID_UPSTREAM_PREDICATE.test(rxNetworkInfo)).isFalse();
    }

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test
    public void shouldBeTrue_whenDownBandwidthSatisfied() throws Exception {
        when(networkCapabilities.getLinkDownstreamBandwidthKbps()).thenReturn(VALID_DOWN_BANDWIDTH);
        when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

        assertThat(VALID_DOWNSTREAM_PREDICATE.test(rxNetworkInfo)).isTrue();
    }

    @RequiresApi(api = LOLLIPOP)
    @Config(sdk = LOLLIPOP)
    @Test
    public void shouldBeFalse_whenDownBandwidthNotSatisfied() throws Exception {
        when(networkCapabilities.getLinkDownstreamBandwidthKbps()).thenReturn(INVALID_DOWN_BANDWIDTH);
        when(rxNetworkInfo.getNetworkCapabilities()).thenReturn(networkCapabilities);

        assertThat(VALID_DOWNSTREAM_PREDICATE.test(rxNetworkInfo)).isFalse();
    }
}
