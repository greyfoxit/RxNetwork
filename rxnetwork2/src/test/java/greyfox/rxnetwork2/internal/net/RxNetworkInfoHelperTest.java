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

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_MOBILE_MMS;
import static android.net.NetworkInfo.DetailedState.CONNECTED;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork2.BuildConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowConnectivityManager;
import org.robolectric.shadows.ShadowNetwork;
import org.robolectric.shadows.ShadowNetworkInfo;

@SuppressWarnings({"WeakerAccess", "deprecation"})
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        shadows = {ShadowConnectivityManager.class, ShadowNetworkInfo.class, ShadowNetwork.class})
public class RxNetworkInfoHelperTest {

    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock Context context;
    @Mock ConnectivityManager connectivityManager;

    Network NETWORK;
    RxNetworkInfo DEFAULT_RXNETWORK_INFO;
    RxNetworkInfo VALID_RXNETWORK_INFO;
    NetworkInfo NETWORK_INFO;

    @Before
    public void setUp() {
        NETWORK_INFO = ShadowNetworkInfo.newInstance(CONNECTED, TYPE_MOBILE, TYPE_MOBILE_MMS,
                true, true);
        DEFAULT_RXNETWORK_INFO = RxNetworkInfo.create();
        VALID_RXNETWORK_INFO = RxNetworkInfo.createFrom(NETWORK_INFO);
        when(context.getSystemService(CONNECTIVITY_SERVICE)).thenReturn(connectivityManager);
    }

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaConstructor() {
        new RxNetworkInfoHelper();
    }

    @Test
    public void shouldNeverBeNull_whenFromContext() {
        RxNetworkInfo sut = RxNetworkInfoHelper.getNetworkInfoFrom(context);

        assertThat(sut).isNotNull();
    }

    @Test
    public void shouldFallbackToDefault_whenActiveNetworkInfoNull() {
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(null);

        RxNetworkInfo sut = RxNetworkInfoHelper.getNetworkInfoFrom(context);

        assertThat(sut).isEqualTo(DEFAULT_RXNETWORK_INFO);
    }

    @Test
    public void shouldReturnProperNetworkInfo_whenFromContext() {
        when(connectivityManager.getActiveNetworkInfo()).thenReturn(NETWORK_INFO);

        RxNetworkInfo sut = RxNetworkInfoHelper.getNetworkInfoFrom(context);

        assertThat(sut).isEqualTo(VALID_RXNETWORK_INFO);
    }

    @Test
    @Config(sdk = LOLLIPOP)
    @RequiresApi(LOLLIPOP)
    public void shouldNeverBeNull_whenFromNetwork() {
        NETWORK = getNetwork();

        RxNetworkInfo sut = RxNetworkInfoHelper.getNetworkInfoFrom(NETWORK, connectivityManager);

        assertThat(sut).isNotNull();
    }

    @Test
    @Config(sdk = LOLLIPOP)
    @RequiresApi(LOLLIPOP)
    public void shouldFallbackToDefault_whenNetworkInfoFromNetworkNull() {
        NETWORK = getNetwork();
        when(connectivityManager.getNetworkInfo(NETWORK)).thenReturn(null);

        RxNetworkInfo sut = RxNetworkInfoHelper.getNetworkInfoFrom(NETWORK, connectivityManager);

        assertThat(sut).isEqualTo(DEFAULT_RXNETWORK_INFO);
    }

    @Test
    @Config(sdk = LOLLIPOP)
    @RequiresApi(LOLLIPOP)
    public void shouldReturnProperNetworkInfo_whenProvidedNetwork() {
        NETWORK = getNetwork();
        when(connectivityManager.getNetworkInfo(NETWORK)).thenReturn(NETWORK_INFO);

        RxNetworkInfo sut = RxNetworkInfoHelper.getNetworkInfoFrom(NETWORK, connectivityManager);

        assertThat(sut).isEqualTo(VALID_RXNETWORK_INFO);
    }

    @RequiresApi(LOLLIPOP)
    private Network getNetwork() {
        return ShadowNetwork.newInstance(NETWORK_INFO.getType());
    }
}
