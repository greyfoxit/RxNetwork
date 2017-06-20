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

import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork.RxNetwork;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.functions.Predicate;
import java.util.Arrays;

import static android.net.ConnectivityManager.TYPE_MOBILE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Contains predefined predicates for filtering reactive streams of {@link RxNetwork}.
 *
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
@RestrictTo(LIBRARY_GROUP)
public final class RxNetworkInfoPredicate {

  @VisibleForTesting
  RxNetworkInfoPredicate() {
    throw new AssertionError("No instances.");
  }

  /**
   * Predicate that returns true if at least one given {@link NetworkInfo.State state} occurred.
   * <p>
   * This can be useful for filtering reactive streams of {@link RxNetwork}, for example:
   * <pre><code>
   * RxNetwork.observe()
   *          .subscribeOn(Schedulers.io())
   *          .filter(hasState(CONNECTED, CONNECTING))
   *          .observeOn(AndroidSchedulers.mainThread())
   *          .subscribe(...);
   * </code></pre>
   *
   * @author Radek Kozak
   */
  public static final class State {

    @VisibleForTesting
    State() {
      throw new AssertionError("No instances.");
    }

    /**
     * Filter for determining if any of provided network states occurred.
     *
     * @param networkStates one or many {@link NetworkInfo.State}
     *
     * @return {@code true} if any of the given network states occurred, {@code false} otherwise
     */
    public static Predicate<RxNetworkInfo> hasState(final NetworkInfo.State... networkStates) {
      return new Predicate<RxNetworkInfo>() {
        @Override
        public boolean test(RxNetworkInfo networkInfo) throws Exception {
          return Arrays.asList(networkStates).contains(networkInfo.getState());
        }
      };
    }
  }

  /**
   * Predicate that returns true if at least one given {@link NetworkInfo#getType} type} occurred.
   * <p>
   * This can be useful for filtering reactive streams of {@link RxNetwork}, for example:
   * <pre><code>
   * RxNetwork.observe()
   *          .subscribeOn(Schedulers.io())
   *          .filter(hasType(TYPE_WIFI, TYPE_MOBILE))
   *          .observeOn(AndroidSchedulers.mainThread())
   *          .subscribe(...);
   * </code></pre>
   *
   * @author Radek Kozak
   */
  public static final class Type {

    public static final Predicate<RxNetworkInfo> IS_MOBILE = isOfTypeMobile();
    public static final Predicate<RxNetworkInfo> IS_WIFI = isOfTypeWifi();

    @VisibleForTesting
    Type() {
      throw new AssertionError("No instances.");
    }

    public static Predicate<RxNetworkInfo> hasType(final int... networkTypes) {
      return new Predicate<RxNetworkInfo>() {
        @Override
        public boolean test(RxNetworkInfo networkInfo) throws Exception {
          for (int type : networkTypes) {
            if (networkInfo.getType() == type) {
              return true;
            }
          }
          return false;
        }
      };
    }

    private static Predicate<RxNetworkInfo> isOfTypeMobile() {
      return new Predicate<RxNetworkInfo>() {
        @Override
        public boolean test(RxNetworkInfo networkInfo) throws Exception {
          return networkInfo.getType() == TYPE_MOBILE;
        }
      };
    }

    private static Predicate<RxNetworkInfo> isOfTypeWifi() {
      return new Predicate<RxNetworkInfo>() {
        @Override
        public boolean test(RxNetworkInfo networkInfo) throws Exception {
          return networkInfo.getType() == TYPE_WIFI;
        }
      };
    }
  }

  @RequiresApi(LOLLIPOP)
  public static final class Capabilities {

    @VisibleForTesting
    Capabilities() {
      throw new AssertionError("No instances");
    }

    public static Predicate<RxNetworkInfo> hasCapability(final int... capabilities) {

      return new Predicate<RxNetworkInfo>() {
        @Override
        public boolean test(RxNetworkInfo networkInfo) throws Exception {
          final NetworkCapabilities networkCapabilities = networkInfo.getNetworkCapabilities();

          if (networkCapabilities != null) {
            for (Integer capability : capabilities) {
              if (networkCapabilities.hasCapability(capability)) {
                return true;
              }
            }
          }

          return false;
        }
      };
    }

    public static Predicate<RxNetworkInfo> hasTransportType(final int... transportTypes) {
      return new Predicate<RxNetworkInfo>() {
        @Override
        public boolean test(RxNetworkInfo networkInfo) throws Exception {
          final NetworkCapabilities networkCapabilities = networkInfo.getNetworkCapabilities();

          if (networkCapabilities != null) {
            for (Integer transportType : transportTypes) {
              if (networkCapabilities.hasTransport(transportType)) {
                return true;
              }
            }
          }

          return false;
        }
      };
    }

    /**
     * Checks if given network satisfies minimum upstream bandwidth.
     * <p>
     * Please understand that upstream bandwidth is never measured, but rather is inferred
     * from technology type and other link parameters as documented in
     * {@link NetworkCapabilities#getLinkUpstreamBandwidthKbps()}
     *
     * @param upBandwidth estimated first hop upstream (device to network) bandwidth in Kbps
     *
     * @return {@code true} if upstream bandwidth is satisfied, {@code false} otherwise
     *
     * @see NetworkCapabilities#getLinkUpstreamBandwidthKbps
     */
    public static Predicate<RxNetworkInfo> isSatisfiedByUpBandwidth(final int upBandwidth) {
      return new Predicate<RxNetworkInfo>() {
        @Override
        public boolean test(RxNetworkInfo networkInfo) throws Exception {
          final NetworkCapabilities networkCapabilities = networkInfo.getNetworkCapabilities();

          return networkCapabilities != null
              && networkCapabilities.getLinkUpstreamBandwidthKbps() >= upBandwidth;
        }
      };
    }

    /**
     * Checks if given network satisfies minimum downstream bandwidth.
     * <p>
     * Please understand that downstream bandwidth is never measured, but rather is inferred
     * from technology type and other link parameters as documented in
     * {@link NetworkCapabilities#getLinkDownstreamBandwidthKbps()}
     *
     * @param downBandwidth estimated first hop downstream (device to network) bandwidth in
     *                      Kbps
     *
     * @return {@code true} if downstream bandwidth is satisfied, {@code false} otherwise
     *
     * @see NetworkCapabilities#getLinkDownstreamBandwidthKbps
     */
    public static Predicate<RxNetworkInfo> isSatisfiedByDownBandwidth(final int downBandwidth) {
      return new Predicate<RxNetworkInfo>() {
        @Override
        public boolean test(RxNetworkInfo networkInfo) throws Exception {
          final NetworkCapabilities networkCapabilities = networkInfo.getNetworkCapabilities();

          return networkCapabilities != null
              && networkCapabilities.getLinkDownstreamBandwidthKbps() >= downBandwidth;
        }
      };
    }
  }
}
