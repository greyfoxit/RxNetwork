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
package com.example.rxnetwork;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import greyfox.rxnetwork2.RxNetwork;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

//import static greyfox.rxnetwork2.internal.strategy.predicate.RxNetworkPredicate.Capabilities.hasCapability;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.networkInfo) TextView netInfo;
    private CompositeDisposable subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        subscriptions = new CompositeDisposable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscriptions.add(rxNetworkSubscription());
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.clear();
    }

    @TargetApi(LOLLIPOP)
    protected Disposable rxNetworkSubscription() {
        return RxNetwork.observe()
                // you can omit setting scheduler every time by providing
                // default scheduler in RxNetwork.init method
                //.subscribeOn(Schedulers.io())
                //.filter(hasCapability(NET_CAPABILITY_NOT_METERED))
                //.filter(hasTransport(TRANSPORT_WIFI))
/*                .filter(hasState(CONNECTED, CONNECTING))
                .filter(hasType(TYPE_WIFI, TYPE_MOBILE))
                .filter(IS_MOBILE).filter(IS_WIFI)*/
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::toastNetworkInfo, this::onError, this::onComplete);
    }

    private void onComplete() {
        Log.d(TAG, "onComplete invoked");
    }

    private void toastNetworkInfo(RxNetworkInfo networkInfo) {
        final String message = "Network connected: " + networkInfo.isConnected();
        netInfo.setText(message);

        Log.d(TAG, "toastNetworkInfo: " + message);
        Toast.makeText(MainActivity.this, "RxNetworkInfo change: "
                + message, Toast.LENGTH_SHORT).show();
    }

    private void onError(Throwable throwable) {
        Log.d(TAG, "onError: " + throwable.getMessage());
    }
}
