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
package greyfox.rxnetwork.helpers.robolectric.shadows;

import android.net.NetworkCapabilities;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;

import static android.net.NetworkCapabilities.NET_CAPABILITY_MMS;
import static android.net.NetworkCapabilities.TRANSPORT_BLUETOOTH;
import static android.net.NetworkCapabilities.TRANSPORT_CELLULAR;
import static android.net.NetworkCapabilities.TRANSPORT_ETHERNET;
import static android.net.NetworkCapabilities.TRANSPORT_VPN;
import static android.net.NetworkCapabilities.TRANSPORT_WIFI;
import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * Shadow for {@link android.net.ConnectivityManager}.
 *
 * @author Radek Kozak
 */
@SuppressWarnings({ "WeakerAccess", "SameParameterValue", "unused" })
@Implements(NetworkCapabilities.class)
@RequiresApi(LOLLIPOP)
public class ShadowNetworkCapabilities {

  public static final int SIGNAL_STRENGTH_UNSPECIFIED = Integer.MIN_VALUE;
  public static final int NET_CAPABILITY_FOREGROUND = 18;
  private static final int MIN_TRANSPORT = TRANSPORT_CELLULAR;
  private static final int MAX_TRANSPORT = TRANSPORT_VPN;
  private static final int MIN_NET_CAPABILITY = NET_CAPABILITY_MMS;
  private static final int MAX_NET_CAPABILITY = NET_CAPABILITY_FOREGROUND;

  private long mNetworkCapabilities;
  private long mTransportTypes;
  private int mLinkUpBandwidthKbps;
  private int mLinkDownBandwidthKbps;
  private String mNetworkSpecifier;
  private int mSignalStrength;

  public static NetworkCapabilities newInstance(long networkCapabilities, long transportTypes,
      int linkUpBandwidthKbps, int linkDownBandwidthKbps, String networkSpecifier,
      int signalStrength) {

    NetworkCapabilities nc = Shadow.newInstanceOf(NetworkCapabilities.class);
    final ShadowNetworkCapabilities capabilities = Shadow.extract(nc);

    capabilities.mNetworkCapabilities = networkCapabilities;
    capabilities.mTransportTypes = transportTypes;
    capabilities.mLinkUpBandwidthKbps = linkUpBandwidthKbps;
    capabilities.mLinkDownBandwidthKbps = linkDownBandwidthKbps;
    capabilities.mNetworkSpecifier = networkSpecifier;
    capabilities.mSignalStrength = signalStrength;
    return nc;
  }

  public static String transportNamesOf(int[] types) {
    StringBuilder transports = new StringBuilder();
    for (int i = 0; i < types.length; ) {
      switch (types[i]) {
        case TRANSPORT_CELLULAR:
          transports.append("CELLULAR");
          break;
        case TRANSPORT_WIFI:
          transports.append("WIFI");
          break;
        case TRANSPORT_BLUETOOTH:
          transports.append("BLUETOOTH");
          break;
        case TRANSPORT_ETHERNET:
          transports.append("ETHERNET");
          break;
        case TRANSPORT_VPN:
          transports.append("VPN");
          break;
      }
      if (++i < types.length) {
        transports.append("|");
      }
    }
    return transports.toString();
  }

  public void addTransportType(int transportType) {
    if (transportType < MIN_TRANSPORT || transportType > MAX_TRANSPORT) {
      throw new IllegalArgumentException("TransportType out of range");
    }
    mTransportTypes |= 1 << transportType;
    setNetworkSpecifier(mNetworkSpecifier); // used for exception checking
  }

  public void removeTransportType(int transportType) {
    if (transportType < MIN_TRANSPORT || transportType > MAX_TRANSPORT) {
      throw new IllegalArgumentException("TransportType out of range");
    }
    mTransportTypes &= ~(1 << transportType);
    setNetworkSpecifier(mNetworkSpecifier); // used for exception checking
  }

  @Implementation
  public int getLinkUpstreamBandwidthKbps() {
    return mLinkUpBandwidthKbps;
  }

  @Implementation
  public void setLinkUpstreamBandwidthKbps(int upKbps) {
    mLinkUpBandwidthKbps = upKbps;
  }

  @Implementation
  public int getLinkDownstreamBandwidthKbps() {
    return mLinkDownBandwidthKbps;
  }

  @Implementation
  public void setLinkDownstreamBandwidthKbps(int downKbps) {
    mLinkDownBandwidthKbps = downKbps;
  }

  @Implementation
  public String getNetworkSpecifier() {
    return mNetworkSpecifier;
  }

  public void setNetworkSpecifier(String networkSpecifier) {
    if (!TextUtils.isEmpty(networkSpecifier) && Long.bitCount(mTransportTypes) != 1) {
      throw new IllegalStateException(
          "Must have a single transport specified to use " + "setNetworkSpecifier");
    }
    mNetworkSpecifier = networkSpecifier;
  }

  private int[] enumerateBits(long val) {
    int size = Long.bitCount(val);
    int[] result = new int[size];
    int index = 0;
    int resource = 0;
    while (val > 0) {
      if ((val & 1) == 1) {
        result[index++] = resource;
      }
      val = val >> 1;
      resource++;
    }
    return result;
  }

  public boolean hasCapability(int capability) {
    return !(capability < MIN_NET_CAPABILITY || capability > MAX_NET_CAPABILITY) && (
        (mNetworkCapabilities & (1 << capability)) != 0);
  }

  @Implementation
  public boolean hasSignalStrength() {
    return mSignalStrength > SIGNAL_STRENGTH_UNSPECIFIED;
  }

  @Implementation
  public int getSignalStrength() {
    return mSignalStrength;
  }

  @Implementation
  public void setSignalStrength(int signalStrength) {
    mSignalStrength = signalStrength;
  }
}
