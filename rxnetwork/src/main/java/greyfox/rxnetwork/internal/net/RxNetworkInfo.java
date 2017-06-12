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

import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.annotation.VisibleForTesting.PRIVATE;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * Decorator class of {@link NetworkInfo}.
 * <p>
 * For pre-Lollipop (API &lt; 21) this class is simple one-to-one wrapper of
 * aforementioned {@link NetworkInfo}.
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

  @VisibleForTesting(otherwise = PRIVATE)
  RxNetworkInfo() {
    throw new AssertionError("Use static factory methods or Builder to create RxNetworkInfo");
  }

  @VisibleForTesting(otherwise = PRIVATE)
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

  public static RxNetworkInfo createFrom(@NonNull NetworkInfo networkInfo) {
    checkNotNull(networkInfo, "networkInfo");
    return builderFrom(networkInfo).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builderFrom(@NonNull NetworkInfo networkInfo) {
    checkNotNull(networkInfo, "networkInfo");

    return new Builder().state(networkInfo.getState()).detailedState(networkInfo.getDetailedState())
                        .type(networkInfo.getType()).subType(networkInfo.getSubtype())
                        .available(networkInfo.isAvailable())
                        .connectedOrConnecting(networkInfo.isConnectedOrConnecting())
                        .connected(networkInfo.isConnected()).failover(networkInfo.isFailover())
                        .roaming(networkInfo.isRoaming()).typeName(networkInfo.getTypeName())
                        .subTypeName(networkInfo.getSubtypeName()).reason(networkInfo.getReason())
                        .extraInfo(networkInfo.getExtraInfo());
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

    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      h *= 1000003;
      h ^= (this.networkCapabilities == null) ? 0 : this.networkCapabilities.hashCode();
    }

    return h;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof RxNetworkInfo) {
      RxNetworkInfo that = (RxNetworkInfo) o;
      final boolean equal = ((this.state == null) ? (that.state == null)
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
          : this.extraInfo.equals(that.extraInfo));

      if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        return equal && ((this.networkCapabilities == null)
            ? (that.networkCapabilities == null)
            : this.networkCapabilities.equals(that.networkCapabilities));
      } else {
        return equal;
      }
    }
    return false;
  }

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

    @RequiresApi(LOLLIPOP) private NetworkCapabilities networkCapabilities;

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
}
