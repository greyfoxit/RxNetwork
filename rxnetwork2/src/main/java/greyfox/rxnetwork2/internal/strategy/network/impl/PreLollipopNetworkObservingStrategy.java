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

import static java.util.logging.Logger.getLogger;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.net.RxNetworkInfoHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import java.util.logging.Logger;

/**
 * RxNetworkInfo observing strategy for pre-Lollipop Android devices (API < 21).
 *
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
public class PreLollipopNetworkObservingStrategy extends BuiltInNetworkObservingStrategy {

    private static final IntentFilter CONNECTIVITY_INTENT_FILTER
            = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

    @NonNull private final Context context;
    private BroadcastReceiver broadcastReceiver;

    public PreLollipopNetworkObservingStrategy(@NonNull Context context) {
        this.context = checkNotNull(context, "context");
    }

    @Override
    public Observable<RxNetworkInfo> observe() {
        return Observable.create(new PreLollipopOnSubscribe()).distinctUntilChanged();
    }

    private void register() {
        context.registerReceiver(broadcastReceiver, CONNECTIVITY_INTENT_FILTER);
    }

    @Override
    void dispose() {
        try {
            context.unregisterReceiver(broadcastReceiver);
        } catch (Exception exc) {
            onError("Could not unregister broadcast receiver", exc);
        }
    }

    @Override
    Logger logger() {
        return getLogger(PreLollipopNetworkObservingStrategy.class.getSimpleName());
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
