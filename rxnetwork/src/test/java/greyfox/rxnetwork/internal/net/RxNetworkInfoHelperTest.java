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
package greyfox.rxnetwork.internal.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork.BuildConfig;
import greyfox.rxnetwork.helpers.robolectric.shadows.ShadowNetworkCapabilities;
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

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_MOBILE_MMS;
import static android.net.NetworkInfo.DetailedState.CONNECTED;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static greyfox.rxnetwork.internal.net.RxNetworkInfo.builderFrom;
import static greyfox.rxnetwork.internal.net.RxNetworkInfoHelper.getRxNetworkInfoFrom;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.robolectric.shadows.ShadowNetworkInfo.newInstance;

@SuppressWarnings({ "WeakerAccess", "deprecation" })
@RequiresApi(LOLLIPOP)
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = LOLLIPOP, shadows = {
    ShadowConnectivityManager.class, ShadowNetworkInfo.class, ShadowNetwork.class,
    ShadowNetworkCapabilities.class
})
public class RxNetworkInfoHelperTest {

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock Context context;
  @Mock ConnectivityManager connectivityManager;

  Network NETWORK;
  NetworkInfo NETWORK_INFO;
  RxNetworkInfo DEFAULT_RXNETWORK_INFO;
  RxNetworkInfo VALID_RXNETWORK_INFO;
  RxNetworkInfo VALID_RXNETWORK_INFO_DETAILED;

  @Before
  public void setUp() {
    DEFAULT_RXNETWORK_INFO = RxNetworkInfo.create();
    NETWORK_INFO = newInstance(CONNECTED, TYPE_MOBILE, TYPE_MOBILE_MMS, true, true);
    RxNetworkInfo.Builder builder = builderFrom(NETWORK_INFO);
    VALID_RXNETWORK_INFO = builder.build();
    NetworkCapabilities networkCapabilities = getNetworkCapabilities();
    VALID_RXNETWORK_INFO_DETAILED = builder.networkCapabilities(networkCapabilities).build();
  }

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateViaConstructor() {
    new RxNetworkInfoHelper();
  }

  @Test
  public void shouldNeverBeNull_whenFromContext() {
    doReturn(connectivityManager).when(context).getSystemService(CONNECTIVITY_SERVICE);

    RxNetworkInfo sut = getRxNetworkInfoFrom(context);

    assertThat(sut).isNotNull();
  }

  @Test
  public void shouldFallbackToDefault_whenActiveNetworkInfoNull() {
    doReturn(connectivityManager).when(context).getSystemService(CONNECTIVITY_SERVICE);
    doReturn(null).when(connectivityManager).getActiveNetworkInfo();

    RxNetworkInfo sut = getRxNetworkInfoFrom(context);

    assertThat(sut).isEqualTo(DEFAULT_RXNETWORK_INFO);
  }

  @Test
  public void shouldReturnProperNetworkInfo_whenFromContext() {
    doReturn(connectivityManager).when(context).getSystemService(CONNECTIVITY_SERVICE);
    doReturn(NETWORK_INFO).when(connectivityManager).getActiveNetworkInfo();

    RxNetworkInfo sut = getRxNetworkInfoFrom(context);

    assertThat(sut).isEqualTo(VALID_RXNETWORK_INFO);
  }

  @Test
  public void shouldNeverBeNull_whenFromNetwork() {
    NETWORK = getNetwork();
    NetworkCapabilities nc = ShadowNetworkCapabilities.newInstance(1, 1, 1, 1, "spec", 1);
    doReturn(nc).when(connectivityManager).getNetworkCapabilities(NETWORK);

    RxNetworkInfo sut = getRxNetworkInfoFrom(NETWORK, connectivityManager);

    assertThat(sut).isNotNull();
  }

  @Test
  public void shouldFallbackToDefault_whenNetworkInfoFromNetworkNull() {
    NETWORK = getNetwork();
    doReturn(null).when(connectivityManager).getNetworkInfo(NETWORK);

    RxNetworkInfo sut = getRxNetworkInfoFrom(NETWORK, connectivityManager);

    assertThat(sut).isEqualTo(DEFAULT_RXNETWORK_INFO);
  }

  @Test
  public void shouldFallbackToDefault_whenNetworkInfoFromNetworkThrows() {
    NETWORK = getNetwork();
    doThrow(Exception.class).when(connectivityManager).getNetworkInfo(NETWORK);

    RxNetworkInfo sut = getRxNetworkInfoFrom(NETWORK, connectivityManager);

    assertThat(sut).isEqualTo(DEFAULT_RXNETWORK_INFO);
  }

  @Test
  public void shouldReturnWithoutNetworkCapabilities_whenGettingNetworkCapabilitiesThrows() {
    NETWORK = getNetwork();
    doThrow(Exception.class).when(connectivityManager).getNetworkCapabilities(NETWORK);

    RxNetworkInfo sut = getRxNetworkInfoFrom(NETWORK, connectivityManager);

    assertThat(sut.getNetworkCapabilities()).isNull();
  }

  @Test
  public void shouldReturnProperNetworkInfo_whenProvidedNetwork() {
    setUpNetworkWithNetworkCapabilities();

    RxNetworkInfo sut = getRxNetworkInfoFrom(NETWORK, connectivityManager);

    assertThat(sut).isEqualTo(VALID_RXNETWORK_INFO_DETAILED);
  }

  private void setUpNetworkWithNetworkCapabilities() {
    NETWORK = getNetwork();
    doReturn(NETWORK_INFO).when(connectivityManager).getNetworkInfo(NETWORK);
    NetworkCapabilities nc = getNetworkCapabilities();
    doReturn(nc).when(connectivityManager).getNetworkCapabilities(NETWORK);
  }

  private Network getNetwork() {
    return ShadowNetwork.newInstance(NETWORK_INFO.getType());
  }

  private NetworkCapabilities getNetworkCapabilities() {
    return ShadowNetworkCapabilities.newInstance(1, 1, 1, 1, "spec", 1);
  }
}
