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
package greyfox.rxnetwork.internal.strategy.network.helpers;

import android.net.NetworkInfo;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.functions.Function;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Predefined collection of {@link RxNetworkInfo} related helper functions.
 *
 * @author Radek Kozak
 */
@RestrictTo(LIBRARY_GROUP)
public final class Functions {

  public static final Function<RxNetworkInfo, Boolean> TO_CONNECTION_STATE = toConnectionState();

  @VisibleForTesting
  Functions() {
    throw new AssertionError("No instances.");
  }

  /**
   * Maps {@link NetworkInfo network} to its {@link Boolean connection state}.
   */
  private static Function<RxNetworkInfo, Boolean> toConnectionState() {
    return new Function<RxNetworkInfo, Boolean>() {
      @Override
      public Boolean apply(RxNetworkInfo networkInfo) throws Exception {
        return networkInfo != null && networkInfo.isConnected();
      }
    };
  }
}
