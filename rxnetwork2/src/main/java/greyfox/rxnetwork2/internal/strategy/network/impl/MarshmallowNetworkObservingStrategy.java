package greyfox.rxnetwork2.internal.strategy.network.impl;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.os.Build.VERSION_CODES.M;

import static java.util.logging.Logger.getLogger;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

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
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.net.RxNetworkInfoHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.subjects.PublishSubject;
import java.util.logging.Logger;

/**
 * RxNetworkInfo observing strategy for Android devices with API 23 (Marshmallow) or higher.
 *
 * @author Radek Kozak
 */
@RequiresApi(M)
public class MarshmallowNetworkObservingStrategy extends BaseNetworkObservingStrategy {

    private static final IntentFilter IDLE_MODE_CHANGED
            = new IntentFilter(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED);

    private static ConnectivityManager.NetworkCallback networkCallback;
    private static BroadcastReceiver idleModeReceiver;

    @NonNull private final ConnectivityManager connectivityManager;
    @NonNull private final PowerManager powerManager;
    @NonNull private final Context context;
    @NonNull private final PublishSubject<RxNetworkInfo> networkChange = PublishSubject.create();

    public MarshmallowNetworkObservingStrategy(@NonNull Context context) {
        this.context = checkNotNull(context, "context");
        connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
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
                upstream.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(context));
            }
        }

        private boolean isDeviceInIdleMode(final Context context) {
            final String packageName = context.getPackageName();

            return powerManager.isDeviceIdleMode()
                    && !powerManager.isIgnoringBatteryOptimizations(packageName);
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
            NetworkRequest request = new NetworkRequest.Builder().build();
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
            upstream.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(network, connectivityManager));
        }

        @Override
        public void onLost(Network network) {
            upstream.onNext(RxNetworkInfoHelper.getNetworkInfoFrom(network, connectivityManager));
        }
    }
}
