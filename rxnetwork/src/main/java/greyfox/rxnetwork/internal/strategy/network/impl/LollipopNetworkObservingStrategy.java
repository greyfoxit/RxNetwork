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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.NetworkRequest;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import java.util.logging.Logger;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * RxNetworkInfo observing strategy for Android devices with API 21 (Lollipop) or higher.
 *
 * @author Radek Kozak
 */
@RequiresApi(LOLLIPOP)
@RestrictTo(LIBRARY_GROUP)
public final class LollipopNetworkObservingStrategy extends Api21BaseNetworkObservingStrategy {

  private final ConnectivityManager connectivityManager;
  @Nullable private NetworkRequest networkRequest;
  private NetworkCallback networkCallback;

  public LollipopNetworkObservingStrategy(@NonNull Context context) {
    checkNotNull(context, "context");
    connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
  }

  public LollipopNetworkObservingStrategy(@NonNull Context context,
      @NonNull NetworkRequest networkRequest) {

    this(context);
    this.networkRequest = checkNotNull(networkRequest, "network request");
  }

  @Override
  public Observable<RxNetworkInfo> observe() {
    return Observable.create(new LollipopOnSubscribe()).distinctUntilChanged();
  }

  private void register() {
    NetworkRequest request =
        networkRequest != null ? networkRequest : new NetworkRequest.Builder().build();
    connectivityManager.registerNetworkCallback(request, networkCallback);
  }

  @Override
  void dispose() {
    try {
      connectivityManager.unregisterNetworkCallback(networkCallback);
    } catch (Exception e) {
      onError("Could not unregister network callback", e);
    }
  }

  @Override
  Logger logger() {
    return Logger.getLogger(LollipopNetworkObservingStrategy.class.getSimpleName());
  }

  @Override
  ConnectivityManager connectivityManager() {
    return this.connectivityManager;
  }

  private final class LollipopOnSubscribe implements ObservableOnSubscribe<RxNetworkInfo> {

    @Override
    public void subscribe(final ObservableEmitter<RxNetworkInfo> upstream) throws Exception {
      networkCallback = new StrategyNetworkCallback(upstream);
      upstream.setCancellable(new StrategyCancellable());
      register();
    }
  }
}
