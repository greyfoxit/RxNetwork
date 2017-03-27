package greyfox.rxnetwork2.internal.strategy.impl;

import static android.os.Build.VERSION_CODES.M;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.net.RxNetworkInfoHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subjects.PublishSubject;

/**
 * RxNetworkInfo observing strategy for Android devices with API 23 (Marshmallow) or higher.
 *
 * @author Radek Kozak
 */
@TargetApi(M)
public class MarshmallowNetworkObservingStrategy extends BuiltInNetworkObservingStrategy {

    private static final String TAG = MarshmallowNetworkObservingStrategy.class.getSimpleName();
    private static final IntentFilter IDLE_MODE_CHANGED
            = new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);
    private static ConnectivityManager.NetworkCallback networkCallback;
    private static BroadcastReceiver idleModeReceiver;
    private final ConnectivityManager manager;
    @NonNull private final Context context;
    private final PublishSubject<RxNetworkInfo> networkChange = PublishSubject.create();

    public MarshmallowNetworkObservingStrategy(@NonNull Context context) {
        this.context = checkNotNull(context, "context == null");
        manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    private void unregisterIdleModeReceiver() {
        try {
            context.unregisterReceiver(idleModeReceiver);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't unregister idle mode broadcast receiver: " + e.getMessage());
        }
    }

    private void unregisterNetworkCallback() {
        try {
            manager.unregisterNetworkCallback(networkCallback);
        } catch (Exception e) {
            Log.e(TAG, "Couldn't unregister network callback: " + e.getMessage());
        }
    }

    @RequiresApi(M)
    private static final class DeviceIdleReceiver extends BroadcastReceiver {

        private final ObservableEmitter<RxNetworkInfo> upstream;

        DeviceIdleReceiver(@NonNull ObservableEmitter<RxNetworkInfo> upstream) {
            this.upstream = checkNotNull(upstream, "upstream == null");
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (isDeviceInIdleMode(context)) {
                upstream.onNext(RxNetworkInfo.create());
            } else {
                upstream.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(context));
            }
        }

        private boolean isDeviceInIdleMode(final Context context) {
            final String packageName = context.getPackageName();
            final PowerManager manager = (PowerManager)
                    context.getSystemService(Context.POWER_SERVICE);

            return manager.isDeviceIdleMode()
                    && !manager.isIgnoringBatteryOptimizations(packageName);
        }
    }

    private final class MarshmallowOnSubscribe implements ObservableOnSubscribe<RxNetworkInfo> {

        @Override
        public void subscribe(@NonNull final ObservableEmitter<RxNetworkInfo> upstream)
                throws Exception {
            checkNotNull(upstream, "upstream == null");
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
            NetworkRequest request = new NetworkRequest.Builder().build();
            manager.registerNetworkCallback(request, networkCallback);
        }
    }

    private final class MarshmallowNetworkCallback extends ConnectivityManager.NetworkCallback {

        final ObservableEmitter<RxNetworkInfo> upstream;

        MarshmallowNetworkCallback(@NonNull ObservableEmitter<RxNetworkInfo> upstream) {
            this.upstream = checkNotNull(upstream, "upstream == null");
        }

        @Override
        public void onAvailable(Network network) {
            //networkChange.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(network, manager));
            upstream.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(network, manager));
        }

        @Override
        public void onLost(Network network) {
            //networkChange.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(network, manager));
            upstream.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(network, manager));
        }
    }
}
