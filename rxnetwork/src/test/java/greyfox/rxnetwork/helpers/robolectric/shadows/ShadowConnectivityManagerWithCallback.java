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

import android.annotation.SuppressLint;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.support.annotation.RequiresApi;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.Shadows;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowNetwork;
import org.robolectric.shadows.ShadowNetworkInfo;

import static android.net.ConnectivityManager.DEFAULT_NETWORK_PREFERENCE;
import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_MOBILE_MMS;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.NetworkInfo.DetailedState.CONNECTED;
import static android.net.NetworkInfo.DetailedState.DISCONNECTED;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static org.robolectric.RuntimeEnvironment.getApiLevel;

/**
 * Shadow for {@link android.net.ConnectivityManager}.
 *
 * @author radekkozak
 */
@SuppressWarnings({ "WeakerAccess", "unused" })
@SuppressLint("UseSparseArrays")
@Implements(ConnectivityManager.class)
public class ShadowConnectivityManagerWithCallback {

  // Package-private for tests.
  private static final int NET_ID_WIFI = TYPE_WIFI;
  private static final int NET_ID_MOBILE = TYPE_MOBILE;

  private final Map<Integer, NetworkInfo> networkTypeToNetworkInfo = new HashMap<>();
  private final Map<Integer, Network> netIdToNetwork = new HashMap<>();
  private final Map<Integer, NetworkInfo> netIdToNetworkInfo = new HashMap<>();

  private NetworkInfo activeNetworkInfo;
  private boolean backgroundDataSetting;
  private int networkPreference = DEFAULT_NETWORK_PREFERENCE;
  private HashSet<NetworkCallback> networkCallbacks = new HashSet<>();

  public ShadowConnectivityManagerWithCallback() {
    NetworkInfo wifi = ShadowNetworkInfo.newInstance(DISCONNECTED, TYPE_WIFI, 0, true, false);
    networkTypeToNetworkInfo.put(TYPE_WIFI, wifi);

    NetworkInfo mobile =
        ShadowNetworkInfo.newInstance(CONNECTED, TYPE_MOBILE, TYPE_MOBILE_MMS, true, true);
    networkTypeToNetworkInfo.put(TYPE_MOBILE, mobile);

    this.activeNetworkInfo = mobile;

    if (getApiLevel() >= LOLLIPOP) {
      netIdToNetwork.put(NET_ID_WIFI, ShadowNetwork.newInstance(NET_ID_WIFI));
      netIdToNetwork.put(NET_ID_MOBILE, ShadowNetwork.newInstance(NET_ID_MOBILE));
      netIdToNetworkInfo.put(NET_ID_WIFI, wifi);
      netIdToNetworkInfo.put(NET_ID_MOBILE, mobile);
    }
  }

  public Set<NetworkCallback> getNetworkCallbacks() {
    return networkCallbacks;
  }

  @Implementation(minSdk = LOLLIPOP)
  @RequiresApi(LOLLIPOP)
  public void registerNetworkCallback(NetworkRequest request, NetworkCallback networkCallback) {
    networkCallbacks.add(networkCallback);
    // simulate available connection
    networkCallback.onAvailable(getActiveNetwork());
  }

  @RequiresApi(LOLLIPOP)
  @Implementation(minSdk = LOLLIPOP)
  public void unregisterNetworkCallback(NetworkCallback networkCallback) {
    if (networkCallback == null) {
      throw new IllegalArgumentException("Invalid NetworkCallback");
    }
    if (networkCallbacks.contains(networkCallback)) {
      networkCallbacks.remove(networkCallback);
      // simulate lost connection
      networkCallback.onLost(getActiveNetwork());
    }
  }

  @Implementation
  public NetworkInfo getActiveNetworkInfo() {
    return activeNetworkInfo;
  }

  public void setActiveNetworkInfo(NetworkInfo info) {
    if (getApiLevel() >= LOLLIPOP) {
      activeNetworkInfo = info;
      if (info != null) {
        networkTypeToNetworkInfo.put(info.getType(), info);
        netIdToNetwork.put(info.getType(), ShadowNetwork.newInstance(info.getType()));
      } else {
        networkTypeToNetworkInfo.clear();
        netIdToNetwork.clear();
      }
    } else {
      activeNetworkInfo = info;
      if (info != null) {
        networkTypeToNetworkInfo.put(info.getType(), info);
      } else {
        networkTypeToNetworkInfo.clear();
      }
    }
  }

  @Implementation(minSdk = M)
  public Network getActiveNetwork() {
    return netIdToNetwork.get(getActiveNetworkInfo().getType());
  }

  @Implementation
  public NetworkInfo[] getAllNetworkInfo() {
    return networkTypeToNetworkInfo.values()
        .toArray(new NetworkInfo[networkTypeToNetworkInfo.size()]);
  }

  @Implementation
  public NetworkInfo getNetworkInfo(int networkType) {
    return networkTypeToNetworkInfo.get(networkType);
  }

  @Implementation(minSdk = LOLLIPOP)
  public NetworkInfo getNetworkInfo(Network network) {
    ShadowNetwork shadowNetwork = Shadows.shadowOf(network);
    return netIdToNetworkInfo.get(shadowNetwork.getNetId());
  }

  @Implementation(minSdk = LOLLIPOP)
  public Network[] getAllNetworks() {
    return netIdToNetwork.values().toArray(new Network[netIdToNetwork.size()]);
  }

  @Implementation
  public boolean getBackgroundDataSetting() {
    return backgroundDataSetting;
  }

  @HiddenApi
  @Implementation
  public void setBackgroundDataSetting(boolean b) {
    backgroundDataSetting = b;
  }

  @Implementation
  public int getNetworkPreference() {
    return networkPreference;
  }

  @Implementation
  public void setNetworkPreference(int preference) {
    networkPreference = preference;
  }

  /**
   * Count {@link ConnectivityManager#TYPE_MOBILE} networks as metered.
   * Other types will be considered unmetered.
   *
   * @return True if the active network is metered.
   */
  @Implementation
  public boolean isActiveNetworkMetered() {
    return activeNetworkInfo != null && activeNetworkInfo.getType() == TYPE_MOBILE;
  }

  public void setNetworkInfo(int networkType, NetworkInfo networkInfo) {
    networkTypeToNetworkInfo.put(networkType, networkInfo);
  }

  /**
   * Adds new {@code network} to the list of all {@link android.net.Network}s.
   *
   * @param network     The network.
   * @param networkInfo The network info paired with the {@link android.net.Network}.
   */
  public void addNetwork(Network network, NetworkInfo networkInfo) {
    ShadowNetwork shadowNetwork = Shadows.shadowOf(network);
    int netId = shadowNetwork.getNetId();
    netIdToNetwork.put(netId, network);
    netIdToNetworkInfo.put(netId, networkInfo);
  }

  /**
   * Removes the {@code network} from the list of all {@link android.net.Network}s.
   *
   * @param network The network.
   */
  public void removeNetwork(Network network) {
    ShadowNetwork shadowNetwork = Shadows.shadowOf(network);
    int netId = shadowNetwork.getNetId();
    netIdToNetwork.remove(netId);
    netIdToNetworkInfo.remove(netId);
  }

  /**
   * Clears the list of all {@link android.net.Network}s.
   */
  public void clearAllNetworks() {
    netIdToNetwork.clear();
    netIdToNetworkInfo.clear();
  }
}

