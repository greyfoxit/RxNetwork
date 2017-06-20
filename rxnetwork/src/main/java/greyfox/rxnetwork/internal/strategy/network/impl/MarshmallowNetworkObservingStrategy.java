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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subjects.PublishSubject;
import java.util.logging.Logger;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.os.Build.VERSION_CODES.M;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;
import static java.util.logging.Logger.getLogger;

/**
 * RxNetworkInfo observing strategy for Android devices with API 23 (Marshmallow) or higher.
 *
 * @author Radek Kozak
 */
@RequiresApi(M)
@RestrictTo(LIBRARY_GROUP)
public final class MarshmallowNetworkObservingStrategy extends BaseNetworkObservingStrategy {

  private static final IntentFilter IDLE_MODE_CHANGED =
      new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);

  @NonNull private final ConnectivityManager connectivityManager;
  @NonNull private final PowerManager powerManager;
  @NonNull private final Context context;
  @NonNull private final PublishSubject<RxNetworkInfo> networkChange = PublishSubject.create();

  private ConnectivityManager.NetworkCallback networkCallback;
  private BroadcastReceiver idleModeReceiver;
  @Nullable private NetworkRequest networkRequest;

  public MarshmallowNetworkObservingStrategy(@NonNull Context context) {
    this.context = checkNotNull(context, "context");
    connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
  }

  public MarshmallowNetworkObservingStrategy(@NonNull Context context,
      @NonNull NetworkRequest networkRequest) {

    this(context);
    this.networkRequest = checkNotNull(networkRequest, "network request");
  }

  @Override
  public Observable<RxNetworkInfo> observe() {
    Observable.create(new MarshmallowOnSubscribe()).subscribeWith(networkChange);
    return networkChange.distinctUntilChanged().doOnDispose(new OnDisposeAction());
  }

  @Override
  void dispose() {
    unregisterNetworkCallback();
    unregisterIdleModeReceiver();
  }

  @Override
  Logger logger() {
    return getLogger(MarshmallowNetworkObservingStrategy.class.getSimpleName());
  }

  private void unregisterIdleModeReceiver() {
    try {
      context.unregisterReceiver(idleModeReceiver);
    } catch (Exception e) {
      onError("Could not unregister idle mode broadcast receiver", e);
    }
  }

  private void unregisterNetworkCallback() {
    try {
      connectivityManager.unregisterNetworkCallback(networkCallback);
    } catch (Exception e) {
      onError("Could not unregister network callback", e);
    }
  }

  @RequiresApi(M)
  private final class DeviceIdleReceiver extends BroadcastReceiver {

    private final ObservableEmitter<RxNetworkInfo> upstream;

    DeviceIdleReceiver(@NonNull ObservableEmitter<RxNetworkInfo> upstream) {
      this.upstream = checkNotNull(upstream, "upstream");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (isDeviceInIdleMode(context)) {
        upstream.onNext(RxNetworkInfo.create());
      } else {
        upstream.onNext(RxNetworkInfo.create(context));
      }
    }

    private boolean isDeviceInIdleMode(final Context context) {
      final String packageName = context.getPackageName();

      return powerManager.isDeviceIdleMode() && !powerManager
          .isIgnoringBatteryOptimizations(packageName);
    }
  }

  private final class MarshmallowOnSubscribe implements ObservableOnSubscribe<RxNetworkInfo> {

    @Override
    public void subscribe(@NonNull final ObservableEmitter<RxNetworkInfo> upstream)
        throws Exception {

      checkNotNull(upstream, "upstream");

      registerIdleModeReceiver(upstream);
      registerNetworkCallback(upstream);
      upstream.setCancellable(new StrategyCancellable());
    }

    private void registerIdleModeReceiver(ObservableEmitter<RxNetworkInfo> upstream) {
      idleModeReceiver = new DeviceIdleReceiver(upstream);
      context.registerReceiver(idleModeReceiver, IDLE_MODE_CHANGED);
    }

    private void registerNetworkCallback(ObservableEmitter<RxNetworkInfo> upstream) {
      networkCallback = new MarshmallowNetworkCallback(upstream);

      NetworkRequest request =
          networkRequest != null ? networkRequest : new NetworkRequest.Builder().build();

      connectivityManager.registerNetworkCallback(request, networkCallback);
    }
  }

  private final class MarshmallowNetworkCallback extends ConnectivityManager.NetworkCallback {

    final ObservableEmitter<RxNetworkInfo> upstream;

    MarshmallowNetworkCallback(@NonNull ObservableEmitter<RxNetworkInfo> upstream) {
      this.upstream = checkNotNull(upstream, "upstream");
    }

    @Override
    public void onAvailable(Network network) {
      upstream.onNext(RxNetworkInfo.create(network, connectivityManager));
    }

    @Override
    public void onLost(Network network) {
      upstream.onNext(RxNetworkInfo.create(network, connectivityManager));
    }
  }
}
