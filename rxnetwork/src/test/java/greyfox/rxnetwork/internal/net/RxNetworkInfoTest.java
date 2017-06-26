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
import android.support.annotation.NonNull;
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
import static android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.net.NetworkInfo.DetailedState.CONNECTED;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static greyfox.rxnetwork.internal.net.RxNetworkInfo.builder;
import static greyfox.rxnetwork.internal.net.RxNetworkInfo.create;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@SuppressWarnings("WeakerAccess, ConstantConditions")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = { KITKAT, LOLLIPOP }, shadows = {
    ShadowConnectivityManager.class, ShadowNetworkInfo.class, ShadowNetwork.class,
    ShadowNetworkCapabilities.class
})
public class RxNetworkInfoTest {

  private static final NetworkInfo NETWORK_INFO =
      ShadowNetworkInfo.newInstance(CONNECTED, TYPE_MOBILE, TYPE_MOBILE_MMS, true, true);

  private static final NetworkInfo NULL_NETWORK_INFO = null;

  private static final NetworkCapabilities NETWORK_CAPABILITIES = ShadowNetworkCapabilities
      .newInstance(NET_CAPABILITY_INTERNET, TRANSPORT_WIFI, 512, 2048, "ssid", 50);

  private static final Object NOT_RXNETWORK_INFO_INSTANCE = new Object();
  private static final RxNetworkInfo DEFAULT_RXNETWORK_INFO = create();

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  private RxNetworkInfo validRxNetworkInfo;
  private RxNetworkInfo validRxNetworkInfoDetailed;
  private Network NETWORK;

  @Mock private Context context;
  @Mock private ConnectivityManager connectivityManager;

  @Before
  public void setUp() {
    RxNetworkInfo.Builder builder = builder(NETWORK_INFO);
    validRxNetworkInfo = builder.build();
    validRxNetworkInfoDetailed = builder.networkCapabilities(NETWORK_CAPABILITIES).build();
  }

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateViaConstructor() {
    new RxNetworkInfo();
  }

  @Test
  public void shouldNeverBeNull() {
    assertThat(create()).isNotNull();
  }

  @Test
  public void shouldBeEqualToSelf() {
    RxNetworkInfo sut = create();

    assertThat(sut).isEqualTo(sut);
  }

  @Test
  public void shouldNotBeEqual_whenNotAnInstanceOfRxNetworkInfo() {
    RxNetworkInfo sut = create();

    assertThat(sut).isNotEqualTo(NOT_RXNETWORK_INFO_INSTANCE);
  }

  @Test
  public void shouldNotBeEqualToNull() {
    RxNetworkInfo sut = create();

    assertThat(sut).isNotEqualTo(null);
  }

  @Test
  public void defaults_shouldBeEqual() {
    RxNetworkInfo rxni = create();
    RxNetworkInfo rxni2 = create();

    assertEqual(rxni, rxni2);
  }

  @Test
  public void shouldBeEqual_whenCreatedFromSameBuilder() {
    final RxNetworkInfo rxni = builder().build();
    final RxNetworkInfo rxni2 = builder().build();

    assertEqual(rxni, rxni2);
  }

  @Test
  public void shouldBeEqual_whenCreatedFromSameDetailedBuilder() {
    RxNetworkInfo rxni = detailedRxNetworkInfoBuilder().build();
    RxNetworkInfo rxni2 = detailedRxNetworkInfoBuilder().build();

    assertEqual(rxni, rxni2);
  }

  @Test
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
  @Config(sdk = LOLLIPOP)
  public void shouldBeEqual_whenCreatedFromSameDetailedBuilder_withNetworkCapabilities() {
    RxNetworkInfo rxni =
        detailedRxNetworkInfoBuilder().networkCapabilities(NETWORK_CAPABILITIES).build();
    RxNetworkInfo rxni2 =
        detailedRxNetworkInfoBuilder().networkCapabilities(NETWORK_CAPABILITIES).build();

    assertEqual(rxni, rxni2);
  }

  @Test
  public void shouldBeEqual_whenCreatedFromSameNetworkInfo() {
    RxNetworkInfo rxni = RxNetworkInfo.create(NETWORK_INFO);
    RxNetworkInfo rxni2 = RxNetworkInfo.create(NETWORK_INFO);

    assertEqual(rxni, rxni2);
  }

  @Test
  public void shouldNotBeEqual() {
    RxNetworkInfo rxni = create();
    RxNetworkInfo rxni2 = RxNetworkInfo.create(NETWORK_INFO);

    assertNotEqual(rxni, rxni2);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenProvidedNullNetworkInfo() {
    RxNetworkInfo.create(NULL_NETWORK_INFO);
  }

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateHelperViaConstructor() {
    new RxNetworkInfo.Helper();
  }

  @Test
  public void shouldNeverBeNull_whenFromContext() {
    doReturn(connectivityManager).when(context).getSystemService(CONNECTIVITY_SERVICE);

    RxNetworkInfo sut = create(context);

    assertThat(sut).isNotNull();
  }

  @Test
  public void shouldFallbackToDefault_whenActiveNetworkInfoNull() {
    doReturn(connectivityManager).when(context).getSystemService(CONNECTIVITY_SERVICE);
    doReturn(null).when(connectivityManager).getActiveNetworkInfo();

    RxNetworkInfo sut = create(context);

    assertThat(sut).isEqualTo(DEFAULT_RXNETWORK_INFO);
  }

  @Test
  public void shouldReturnProperNetworkInfo_whenFromContext() {
    doReturn(connectivityManager).when(context).getSystemService(CONNECTIVITY_SERVICE);
    doReturn(NETWORK_INFO).when(connectivityManager).getActiveNetworkInfo();

    RxNetworkInfo sut = create(context);

    assertThat(sut).isEqualTo(validRxNetworkInfo);
  }

  @Test
  @Config(sdk = LOLLIPOP)
  public void shouldNeverBeNull_whenFromNetwork() {
    NETWORK = getNetwork();
    doReturn(NETWORK_CAPABILITIES).when(connectivityManager).getNetworkCapabilities(NETWORK);

    RxNetworkInfo sut = create(NETWORK, connectivityManager);

    assertThat(sut).isNotNull();
  }

  @Test
  @Config(sdk = LOLLIPOP)
  public void shouldFallbackToDefault_whenNetworkInfoFromNetworkNull() {
    NETWORK = getNetwork();
    doReturn(null).when(connectivityManager).getNetworkInfo(NETWORK);

    RxNetworkInfo sut = create(NETWORK, connectivityManager);

    assertThat(sut).isEqualTo(DEFAULT_RXNETWORK_INFO);
  }

  @Test
  @Config(sdk = LOLLIPOP)
  public void shouldFallbackToDefault_whenNetworkInfoFromNetworkThrows() {
    NETWORK = getNetwork();
    doThrow(Exception.class).when(connectivityManager).getNetworkInfo(NETWORK);

    RxNetworkInfo sut = create(NETWORK, connectivityManager);

    assertThat(sut).isEqualTo(DEFAULT_RXNETWORK_INFO);
  }

  @Test
  @Config(sdk = LOLLIPOP)
  public void shouldReturnWithoutNetworkCapabilities_whenGettingNetworkCapabilitiesThrows() {
    NETWORK = getNetwork();
    doThrow(Exception.class).when(connectivityManager).getNetworkCapabilities(NETWORK);

    RxNetworkInfo sut = create(NETWORK, connectivityManager);

    assertThat(sut.getNetworkCapabilities()).isNull();
  }

  @Test
  @Config(sdk = LOLLIPOP)
  public void shouldReturnProperNetworkInfo_whenProvidedNetwork() {
    setUpNetworkWithNetworkCapabilities();

    RxNetworkInfo sut = create(NETWORK, connectivityManager);

    assertThat(sut).isEqualTo(validRxNetworkInfoDetailed);
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
    return builder().available(true).connected(true).connectedOrConnecting(true)
        .detailedState(CONNECTED).extraInfo("extra info").failover(true).reason("some reason")
        .roaming(true).state(NetworkInfo.State.CONNECTED).subType(0).subTypeName("type one").type(1)
        .typeName("type one");
  }

  private RxNetworkInfo.Builder detailedRxNetworkInfoBuilderWithNullsAndOpposites() {
    return builder().available(false).connected(false).connectedOrConnecting(false)
        .detailedState(null).extraInfo(null).failover(false).reason(null).roaming(false).state(null)
        .subType(0).subTypeName(null).type(1).typeName(null);
  }

  private Network getNetwork() {
    return ShadowNetwork.newInstance(NETWORK_INFO.getType());
  }

  private void setUpNetworkWithNetworkCapabilities() {
    NETWORK = getNetwork();
    doReturn(NETWORK_INFO).when(connectivityManager).getNetworkInfo(NETWORK);
    doReturn(NETWORK_CAPABILITIES).when(connectivityManager).getNetworkCapabilities(NETWORK);
  }
}
