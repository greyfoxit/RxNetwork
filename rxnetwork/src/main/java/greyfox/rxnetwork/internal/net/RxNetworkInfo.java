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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import java.util.logging.Logger;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;
import static greyfox.rxnetwork.internal.net.RxNetworkInfo.Helper.getCapabilities;
import static greyfox.rxnetwork.internal.net.RxNetworkInfo.Helper.getNetworkInfo;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

/**
 * Wrapper class for {@link NetworkInfo}.
 * <p>
 * For pre-Lollipop (API &lt; 21) this class is simple one-to-one wrapper of {@link NetworkInfo}.
 * <p>
 * Starting from {@link android.os.Build.VERSION_CODES#LOLLIPOP Lollipop} (API &gt;= 21)
 * it provides additional {@link NetworkCapabilities} information to the class.
 *
 * @author Radek Kozak
 */
@SuppressWarnings({ "unused", "WeakerAccess" })
public class RxNetworkInfo {

  private final NetworkInfo.State state;
  private final NetworkInfo.DetailedState detailedState;
  private final int type;
  private final int subType;
  private final boolean available;
  private final boolean connectedOrConnecting;
  private final boolean connected;
  private final boolean failover;
  private final boolean roaming;
  private final String typeName;
  private final String subTypeName;
  private final String reason;
  private final String extraInfo;
  private final NetworkCapabilities networkCapabilities;

  @VisibleForTesting
  RxNetworkInfo() {
    throw new AssertionError("Use static factory methods or Builder to create RxNetworkInfo");
  }

  @VisibleForTesting
  RxNetworkInfo(@NonNull Builder builder) {
    checkNotNull(builder, "builder");

    state = builder.state;
    detailedState = builder.detailedState;
    type = builder.type;
    subType = builder.subType;
    available = builder.available;
    connectedOrConnecting = builder.connectedOrConnecting;
    connected = builder.connected;
    failover = builder.failover;
    roaming = builder.roaming;
    typeName = builder.typeName;
    subTypeName = builder.subTypeName;
    reason = builder.reason;
    extraInfo = builder.extraInfo;
    networkCapabilities = builder.networkCapabilities;
  }

  public static RxNetworkInfo create() {
    return new Builder().build();
  }

  public static RxNetworkInfo create(@NonNull NetworkInfo networkInfo) {
    checkNotNull(networkInfo, "networkInfo");
    return builder(networkInfo).build();
  }

  /**
   * Gets {@link RxNetworkInfo network information} from provided {@link Context context}.
   *
   * @param context {@link Context}
   *
   * @return {@link RxNetworkInfo} instance
   */
  public static RxNetworkInfo create(@NonNull Context context) {
    checkNotNull(context, "context");

    final ConnectivityManager manager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    final NetworkInfo networkInfo = manager.getActiveNetworkInfo();

    return networkInfo == null ? create() : create(networkInfo);
  }

  /**
   * Gets {@link RxNetworkInfo network information} from given {@link Network} instance
   * along with {@link NetworkCapabilities} provided at the time of registering network callback
   * in {@linkplain ConnectivityManager#registerNetworkCallback} (if available).
   *
   * @param network             {@link Network}
   * @param connectivityManager {@link ConnectivityManager}
   *
   * @return {@link RxNetworkInfo} instance
   */
  @RequiresApi(LOLLIPOP)
  public static RxNetworkInfo create(@NonNull Network network,
      @NonNull ConnectivityManager connectivityManager) {

    checkNotNull(network, "network");
    checkNotNull(connectivityManager, "manager");

    final NetworkInfo networkInfo = getNetworkInfo(network, connectivityManager);
    final NetworkCapabilities capabilities = getCapabilities(network, connectivityManager);

    return networkInfo != null ? builder(networkInfo).networkCapabilities(capabilities).build()
                               : create();
  }

  public static Builder builder() {
    return new Builder();
  }

  static Builder builder(@NonNull NetworkInfo networkInfo) {
    checkNotNull(networkInfo, "networkInfo");

    return new Builder().state(networkInfo.getState()).detailedState(networkInfo.getDetailedState())
        .type(networkInfo.getType()).subType(networkInfo.getSubtype())
        .available(networkInfo.isAvailable()).connected(networkInfo.isConnected())
        .connectedOrConnecting(networkInfo.isConnectedOrConnecting())
        .failover(networkInfo.isFailover()).roaming(networkInfo.isRoaming())
        .typeName(networkInfo.getTypeName()).subTypeName(networkInfo.getSubtypeName())
        .reason(networkInfo.getReason()).extraInfo(networkInfo.getExtraInfo());
  }

  /** @see NetworkInfo#getState() */
  public NetworkInfo.State getState() {
    return state;
  }

  /** @see NetworkInfo#getDetailedState() */
  public NetworkInfo.DetailedState getDetailedState() {
    return detailedState;
  }

  /** @see NetworkInfo#getType() */
  public int getType() {
    return type;
  }

  /** @see NetworkInfo#getSubtype() */
  public int getSubType() {
    return subType;
  }

  /** @see NetworkInfo#isAvailable() */
  public boolean isAvailable() {
    return available;
  }

  /** @see NetworkInfo#isFailover() */
  public boolean isFailover() {
    return failover;
  }

  /** @see NetworkInfo#isRoaming() */
  public boolean isRoaming() {
    return roaming;
  }

  /** @see NetworkInfo#getTypeName() */
  public String getTypeName() {
    return typeName;
  }

  /** @see NetworkInfo#getSubtypeName() */
  public String getSubTypeName() {
    return subTypeName;
  }

  /** @see NetworkInfo#isConnectedOrConnecting() */
  public boolean isConnectedOrConnecting() {
    return connectedOrConnecting;
  }

  /** @see NetworkInfo#isConnected() */
  public boolean isConnected() {
    return connected;
  }

  /** @see NetworkInfo#getReason() */
  public String getReason() {
    return reason;
  }

  /** @see NetworkInfo#getExtraInfo() */
  public String getExtraInfo() {
    return extraInfo;
  }

  /** @see NetworkCapabilities */
  @RequiresApi(LOLLIPOP)
  public NetworkCapabilities getNetworkCapabilities() {
    return networkCapabilities;
  }

  // @formatter:off

  @Override
  public int hashCode() {
    int h = 1;
    h *= 1000003;
    h ^= (this.state == null) ? 0 : this.state.hashCode();
    h *= 1000003;
    h ^= (this.detailedState == null) ? 0 : this.detailedState.hashCode();
    h *= 1000003;
    h ^= this.type;
    h *= 1000003;
    h ^= this.subType;
    h *= 1000003;
    h ^= this.available ? 1231 : 1237;
    h *= 1000003;
    h ^= this.connectedOrConnecting ? 1231 : 1237;
    h *= 1000003;
    h ^= this.connected ? 1231 : 1237;
    h *= 1000003;
    h ^= this.failover ? 1231 : 1237;
    h *= 1000003;
    h ^= this.roaming ? 1231 : 1237;
    h *= 1000003;
    h ^= (this.typeName == null) ? 0 : this.typeName.hashCode();
    h *= 1000003;
    h ^= (this.subTypeName == null) ? 0 : this.subTypeName.hashCode();
    h *= 1000003;
    h ^= (this.reason == null) ? 0 : this.reason.hashCode();
    h *= 1000003;
    h ^= (this.extraInfo == null) ? 0 : this.extraInfo.hashCode();
    h *= 1000003;
    h ^= (this.networkCapabilities == null) ? 0 : this.networkCapabilities.hashCode();

    return h;
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof RxNetworkInfo) {
      RxNetworkInfo that = (RxNetworkInfo) o;

      return ((this.state == null) ? (that.state == null)
                                   : this.state.equals(that.state))

          && ((this.detailedState == null) ? (that.detailedState == null)
                                           : this.detailedState.equals(that.detailedState))

          && (this.type == that.type)
          && (this.subType == that.subType)
          && (this.available == that.available)
          && (this.connectedOrConnecting == that.connectedOrConnecting)
          && (this.connected == that.connected)
          && (this.failover == that.failover)
          && (this.roaming == that.roaming)

          && ((this.typeName == null) ? (that.typeName == null)
                                      : this.typeName.equals(that.typeName))

          && ((this.subTypeName == null) ? (that.subTypeName == null)
                                         : this.subTypeName.equals(that.subTypeName))

          && ((this.reason == null) ? (that.reason == null)
                                    : this.reason.equals(that.reason))

          && ((this.extraInfo == null) ? (that.extraInfo == null)
                                       : this.extraInfo.equals(that.extraInfo))

          && ((this.networkCapabilities == null) ? (that.networkCapabilities == null)
                                                 : this.networkCapabilities.equals(
                                                     that.networkCapabilities));
    }

    return false;
  }

  // @formatter:off

  @SuppressLint("NewApi")
  @Override
  public String toString() {
    return "RxNetworkInfo{"
        + "state=" + state + ", "
        + "detailedState=" + detailedState + ", "
        + "type=" + type + ", "
        + "subType=" + subType + ", "
        + "available=" + available + ", "
        + "connectedOrConnecting=" + connectedOrConnecting + ", "
        + "connected=" + connected + ", "
        + "failover=" + failover + ", "
        + "roaming=" + roaming + ", "
        + "typeName=" + typeName + ", "
        + "subTypeName=" + subTypeName + ", "
        + "reason=" + reason + ", "
        + "extraInfo=" + extraInfo + ", "
        + "networkCapabilities=" + networkCapabilities
        + "}";
  }

  // @formatter:on

  @SuppressWarnings("WeakerAccess")
  public static final class Builder {

    private static final int TYPE_UNKNOWN = -1;
    private static final String NAME_UNKNOWN = "unknown";

    private NetworkInfo.State state = NetworkInfo.State.UNKNOWN;
    private NetworkInfo.DetailedState detailedState = NetworkInfo.DetailedState.IDLE;
    private int type = TYPE_UNKNOWN;
    private int subType = TYPE_UNKNOWN;
    private boolean available;
    private boolean connectedOrConnecting;
    private boolean connected;
    private boolean failover;
    private boolean roaming;
    private String typeName = NAME_UNKNOWN;
    private String subTypeName = NAME_UNKNOWN;
    private String reason = "";
    private String extraInfo = "";

    private NetworkCapabilities networkCapabilities;

    Builder() {
    }

    public Builder state(NetworkInfo.State state) {
      this.state = state;
      return this;
    }

    public Builder detailedState(NetworkInfo.DetailedState detailedState) {
      this.detailedState = detailedState;
      return this;
    }

    public Builder type(int type) {
      this.type = type;
      return this;
    }

    public Builder subType(int subType) {
      this.subType = subType;
      return this;
    }

    public Builder available(boolean available) {
      this.available = available;
      return this;
    }

    public Builder connectedOrConnecting(boolean connectedOrConnecting) {
      this.connectedOrConnecting = connectedOrConnecting;
      return this;
    }

    public Builder connected(boolean connected) {
      this.connected = connected;
      return this;
    }

    public Builder failover(boolean failover) {
      this.failover = failover;
      return this;
    }

    public Builder roaming(boolean roaming) {
      this.roaming = roaming;
      return this;
    }

    public Builder typeName(String typeName) {
      this.typeName = typeName;
      return this;
    }

    public Builder subTypeName(String subTypeName) {
      this.subTypeName = subTypeName;
      return this;
    }

    public Builder reason(String reason) {
      this.reason = reason;
      return this;
    }

    public Builder extraInfo(String extraInfo) {
      this.extraInfo = extraInfo;
      return this;
    }

    @RequiresApi(LOLLIPOP)
    public Builder networkCapabilities(NetworkCapabilities networkCapabilities) {
      this.networkCapabilities = networkCapabilities;
      return this;
    }

    public RxNetworkInfo build() {
      return new RxNetworkInfo(this);
    }
  }

  static final class Helper {

    private static final Logger logger = getLogger(Helper.class.getSimpleName());

    @VisibleForTesting
    Helper() {
      throw new AssertionError("No instances.");
    }

    @RequiresApi(LOLLIPOP)
    @Nullable
    static NetworkCapabilities getCapabilities(@NonNull Network network,
        @NonNull ConnectivityManager connectivityManager) {

      NetworkCapabilities networkCapabilities = null;

      try {
        networkCapabilities = connectivityManager.getNetworkCapabilities(network);
      } catch (Exception exc) {
        logger.log(WARNING,
            "Could not retrieve network capabilities from provided network: " + exc.getMessage());
      }

      return networkCapabilities;
    }

    @RequiresApi(LOLLIPOP)
    @Nullable
    static NetworkInfo getNetworkInfo(@NonNull Network network,
        @NonNull ConnectivityManager connectivityManager) {

      NetworkInfo networkInfo = null;

      try {
        networkInfo = connectivityManager.getNetworkInfo(network);
      } catch (Exception exc) {
        logger.log(WARNING,
            "Could not retrieve network info from provided network: " + exc.getMessage());
      }

      return networkInfo;
    }
  }
}
