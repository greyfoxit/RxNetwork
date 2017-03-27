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
package greyfox.rxnetwork2.internal.strategy.network.impl;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.util.Log;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.net.RxNetworkInfoHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * RxNetworkInfo observing strategy for pre-Lollipop Android devices (API < 21).
 *
 * @author Radek Kozak
 */
public class PreLollipopNetworkObservingStrategy extends BuiltInNetworkObservingStrategy {

    private static final String TAG = PreLollipopNetworkObservingStrategy.class.getSimpleName();
    private static final IntentFilter CONNECTIVITY_FILTER
            = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    private final Context context;
    private BroadcastReceiver broadcastReceiver;

    public PreLollipopNetworkObservingStrategy(@NonNull Context context) {
        this.context = checkNotNull(context, "context == null");
    }

    @Override
    public Observable<RxNetworkInfo> observe() {
        return Observable.create(new PreLollipopOnSubscribe()).distinctUntilChanged();
    }

    private void register() {
        context.registerReceiver(broadcastReceiver, CONNECTIVITY_FILTER);
    }

    @Override
    void dispose() {
        try {
            context.unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't unregister broadcast receiver: " + e.getMessage());
        }
    }

    private final class PreLollipopOnSubscribe implements ObservableOnSubscribe<RxNetworkInfo> {

        @Override
        public void subscribe(final ObservableEmitter<RxNetworkInfo> emitter) throws Exception {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    emitter.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(context));
                }
            };
            emitter.setCancellable(new StrategyCancellable());
            register();
        }
    }
}
