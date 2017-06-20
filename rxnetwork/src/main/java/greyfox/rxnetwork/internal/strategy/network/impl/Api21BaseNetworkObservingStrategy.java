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
package greyfox.rxnetwork.internal.strategy.network.impl;

import android.net.ConnectivityManager;
import android.net.Network;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.ObservableEmitter;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * @author Radek Kozak
 */
@RequiresApi(LOLLIPOP)
abstract class Api21BaseNetworkObservingStrategy extends BaseNetworkObservingStrategy {

  abstract ConnectivityManager connectivityManager();

  final class StrategyNetworkCallback extends ConnectivityManager.NetworkCallback {

    final ObservableEmitter<RxNetworkInfo> upstream;

    StrategyNetworkCallback(@NonNull ObservableEmitter<RxNetworkInfo> upstream) {
      this.upstream = checkNotNull(upstream, "upstream");
    }

    @Override
    public void onAvailable(Network network) {
      upstream.onNext(RxNetworkInfo.create(network, connectivityManager()));
    }

    @Override
    public void onLost(Network network) {
      upstream.onNext(RxNetworkInfo.create(network, connectivityManager()));
    }
  }
}
