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
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import greyfox.rxnetwork.RxNetwork;
import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import javax.inject.Inject;
import toothpick.Toothpick;

@SuppressWarnings("NullableProblems")
public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Inject @NonNull public RxNetwork rxNetwork;
    @BindView(R.id.networkInfo) TextView netInfo;

    private CompositeDisposable subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toothpick.inject(this, Toothpick.openScopes(RxNetwork.class, this));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        subscriptions = new CompositeDisposable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        subscriptions.add(rxNetworkSubscription());
        //subscriptions.add(rxRealInternetAccessSubscription());
    }

    @Override
    protected void onPause() {
        super.onPause();
        subscriptions.clear();
    }

    @NonNull
    @TargetApi(LOLLIPOP)
    protected Disposable rxNetworkSubscription() {
        return rxNetwork.observe()
                .observeOn(AndroidSchedulers.mainThread())
                //.filter(hasCapability(NET_CAPABILITY_NOT_METERED))
                .subscribe(this::toastNetworkInfo, this::onError, this::onComplete);
    }

    @NonNull
    protected Disposable rxRealInternetAccessSubscription() {
        return rxNetwork.observeReal()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::toastInternetConnection, this::onError, this::onComplete);
    }

    private void toastInternetConnection(Boolean connected) {
        final String message = "Internet access: " + connected;
        Log.d(TAG, message);
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void onComplete() {
        Log.d(TAG, "onComplete invoked");
    }

    private void toastNetworkInfo(@NonNull RxNetworkInfo networkInfo) {
        final String message = "Network connected: " + networkInfo.isConnected();
        netInfo.setText(message);

        Log.d(TAG, "toastNetworkInfo: " + message);
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void onError(@NonNull Throwable throwable) {
        Log.d(TAG, "onError: " + throwable.getMessage());
    }
}
