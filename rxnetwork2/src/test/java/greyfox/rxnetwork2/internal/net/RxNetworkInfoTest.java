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
import static android.net.NetworkInfo.DetailedState.CONNECTED;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.net.NetworkInfo;
import greyfox.rxnetwork2.BuildConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowNetworkInfo;

@SuppressWarnings("WeakerAccess")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = {KITKAT, LOLLIPOP})
public class RxNetworkInfoTest {

    static NetworkInfo NETWORK_INFO
            = ShadowNetworkInfo.newInstance(CONNECTED, TYPE_WIFI, 0, true, true);

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
        final RxNetworkInfo sut = RxNetworkInfo.create();

        assertThat(sut).isEqualTo(sut).isEqualToComparingFieldByField(sut);
    }

    @Test
    public void shouldBeEqual_whenCreatedFromSameBuilder() {
        final RxNetworkInfo.Builder builder = RxNetworkInfo.builder();

        RxNetworkInfo rxni = new RxNetworkInfo(builder);
        RxNetworkInfo rxni2 = new RxNetworkInfo(builder);

        assertEqual(rxni, rxni2);
    }

    @Test
    public void shouldBeEqual_whenCreatedFromDefaultBuilder() {
        RxNetworkInfo rxni = new RxNetworkInfo(RxNetworkInfo.builder());
        RxNetworkInfo rxni2 = new RxNetworkInfo(RxNetworkInfo.builder());

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

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenProvidedNullNetworkInfo() {
        RxNetworkInfo.createFrom(null);
    }

    private void assertEqual(RxNetworkInfo rxni, RxNetworkInfo rxni2) {
        assertThat(rxni).isEqualTo(rxni2);
        assertThat(rxni.hashCode()).isEqualTo(rxni2.hashCode());
        assertThat(rxni.toString()).isEqualTo(rxni2.toString());
    }

    private void assertNotEqual(RxNetworkInfo rxni, RxNetworkInfo rxni2) {
        assertThat(rxni).isNotEqualTo(rxni2);
        assertThat(rxni.hashCode()).isNotEqualTo(rxni2.hashCode());
        assertThat(rxni.toString()).isNotEqualTo(rxni2.toString());
    }
}
