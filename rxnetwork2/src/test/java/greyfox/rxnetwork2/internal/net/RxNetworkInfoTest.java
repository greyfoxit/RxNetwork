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
package greyfox.rxnetwork2.internal.net;

import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.net.NetworkInfo.DetailedState.CONNECTED;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork2.BuildConfig;
import greyfox.rxnetwork2.helpers.robolectric.shadows.ShadowNetworkCapabilities;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNetworkInfo;

@SuppressWarnings("WeakerAccess, ConstantConditions")
@RequiresApi(LOLLIPOP)
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP, shadows = ShadowNetworkCapabilities.class)
public class RxNetworkInfoTest {

    NetworkInfo NETWORK_INFO = ShadowNetworkInfo.newInstance(CONNECTED, TYPE_WIFI, 0, true, true);
    NetworkCapabilities NETWORK_CAPABILITIES = ShadowNetworkCapabilities.newInstance(
            NET_CAPABILITY_INTERNET, TRANSPORT_WIFI, 512, 2048, "ssid", 50);
    Object NOT_RXNETWORK_INFO_INSTANCE = new Object();

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaConstructor() {
        new RxNetworkInfo();
    }

    @Test
    public void shouldNeverBeNull() {
        assertThat(RxNetworkInfo.create()).isNotNull();
    }

    @Test
    public void shouldBeEqualToSelf() {
        RxNetworkInfo sut = RxNetworkInfo.create();

        assertThat(sut).isEqualTo(sut);
    }

    @Test
    public void shouldNotBeEqual_whenNotAnInstanceOfRxNetworkInfo() {
        RxNetworkInfo sut = RxNetworkInfo.create();

        assertThat(sut).isNotEqualTo(NOT_RXNETWORK_INFO_INSTANCE);
    }

    @Test
    public void shouldNotBeEqualToNull() {
        RxNetworkInfo sut = RxNetworkInfo.create();

        assertThat(sut).isNotEqualTo(null);
    }

    @Test
    public void defaults_shouldBeEqual() {
        RxNetworkInfo rxni = RxNetworkInfo.create();
        RxNetworkInfo rxni2 = RxNetworkInfo.create();

        assertEqual(rxni, rxni2);
    }

    @Test
    public void shouldBeEqual_whenCreatedFromSameBuilder() {
        final RxNetworkInfo rxni = RxNetworkInfo.builder().build();
        final RxNetworkInfo rxni2 = RxNetworkInfo.builder().build();

        assertEqual(rxni, rxni2);
    }

    @Test
    @Config(sdk = {KITKAT, LOLLIPOP})
    public void shouldBeEqual_whenCreatedFromSameDetailedBuilder() {
        RxNetworkInfo rxni = detailedRxNetworkInfoBuilder().build();
        RxNetworkInfo rxni2 = detailedRxNetworkInfoBuilder().build();

        assertEqual(rxni, rxni2);
    }

    @Test
    @Config(sdk = {KITKAT, LOLLIPOP})
    public void shouldBeEqual_whenCreatedFromSameDetailedBuilder_withNullsAndOpposites() {
        RxNetworkInfo rxni = detailedRxNetworkInfoBuilderWithNullsAndOpposites().build();
        RxNetworkInfo rxni2 = detailedRxNetworkInfoBuilderWithNullsAndOpposites().build();

        assertEqual(rxni, rxni2);
    }

    @Test
    public void shouldBeEqual_whenCreatedFromSameDetailedBuilder_withNullNetworkCapabilities() {
        RxNetworkInfo rxni = detailedRxNetworkInfoBuilder().networkCapabilities(null).build();
        RxNetworkInfo rxni2 = detailedRxNetworkInfoBuilder().networkCapabilities(null).build();

        assertEqual(rxni, rxni2);
    }

    @Test
    public void shouldBeEqual_whenCreatedFromSameDetailedBuilder_withNetworkCapabilities() {
        //NetworkCapabilities nc = buildNetworkCapabilities();

        RxNetworkInfo rxni = detailedRxNetworkInfoBuilder()
                .networkCapabilities(NETWORK_CAPABILITIES).build();
        RxNetworkInfo rxni2 = detailedRxNetworkInfoBuilder()
                .networkCapabilities(NETWORK_CAPABILITIES).build();

        assertEqual(rxni, rxni2);
    }

    @Test
    public void shouldBeEqual_whenCreatedFromSameNetworkInfo() {
        RxNetworkInfo rxni = RxNetworkInfo.createFrom(NETWORK_INFO);
        RxNetworkInfo rxni2 = RxNetworkInfo.createFrom(NETWORK_INFO);

        assertEqual(rxni, rxni2);
    }

    @Test
    public void shouldNotBeEqual() {
        RxNetworkInfo rxni = RxNetworkInfo.create();
        RxNetworkInfo rxni2 = RxNetworkInfo.createFrom(NETWORK_INFO);

        assertNotEqual(rxni, rxni2);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenProvidedNullNetworkInfo() {
        RxNetworkInfo.createFrom(null);
    }

    private void assertEqual(@NonNull RxNetworkInfo rxni, @NonNull RxNetworkInfo rxni2) {
        assertThat(rxni).isNotNull();
        assertThat(rxni2).isNotNull();
        assertThat(rxni).isEqualTo(rxni2).isEqualToComparingFieldByField(rxni2);
        assertThat(rxni.equals(rxni2)).isTrue();
        assertThat(rxni.hashCode()).isEqualTo(rxni2.hashCode());
        assertThat(rxni.toString()).isEqualTo(rxni2.toString());
    }

    private void assertNotEqual(@NonNull RxNetworkInfo rxni, @NonNull RxNetworkInfo rxni2) {
        assertThat(rxni).isNotNull();
        assertThat(rxni2).isNotNull();
        assertThat(rxni).isNotEqualTo(rxni2);
        assertThat(rxni.equals(rxni2)).isFalse();
        assertThat(rxni.hashCode()).isNotEqualTo(rxni2.hashCode());
        assertThat(rxni.toString()).isNotEqualTo(rxni2.toString());
    }

    private RxNetworkInfo.Builder detailedRxNetworkInfoBuilder() {
        return RxNetworkInfo.builder().available(true).connected(true)
                .connectedOrConnecting(true).detailedState(CONNECTED).extraInfo("extra info")
                .failover(true).reason("some reason").roaming(true)
                .state(NetworkInfo.State.CONNECTED).subType(0).subTypeName("type one")
                .type(1).typeName("type one");
    }

    private RxNetworkInfo.Builder detailedRxNetworkInfoBuilderWithNullsAndOpposites() {
        return RxNetworkInfo.builder().available(false).connected(false)
                .connectedOrConnecting(false).detailedState(null).extraInfo(null)
                .failover(false).reason(null).roaming(false)
                .state(null).subType(0).subTypeName(null)
                .type(1).typeName(null);
    }

    private NetworkCapabilities buildNetworkCapabilities() {
        int upBandwidth = 512;
        int downBandwidth = 2048;
        String ssid = "valid ssid";
        int signalStrength = 50;

        return ShadowNetworkCapabilities.newInstance(NET_CAPABILITY_INTERNET, TRANSPORT_WIFI,
                upBandwidth, downBandwidth, ssid, signalStrength);
    }
}
