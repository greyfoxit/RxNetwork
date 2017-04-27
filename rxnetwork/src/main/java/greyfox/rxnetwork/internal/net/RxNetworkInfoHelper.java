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
package greyfox.rxnetwork.internal.net;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.VisibleForTesting;
import java.util.logging.Logger;

/**
 * Helper class for getting {@link RxNetworkInfo network information} from given resources.
 *
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
public final class RxNetworkInfoHelper {

    private static final Logger logger = getLogger(RxNetworkInfoHelper.class.getSimpleName());

    @VisibleForTesting(otherwise = PRIVATE)
    RxNetworkInfoHelper()

    {
        throw new AssertionError("No instances.");
    }

    /**
     * Gets {@link RxNetworkInfo network information} from provided {@link Context context}.
     *
     * @param context {@link Context}
     *
     * @return {@link RxNetworkInfo} instance
     */
    public static RxNetworkInfo getRxNetworkInfoFrom(@NonNull final Context context) {
        checkNotNull(context, "context");

        final ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo == null ? RxNetworkInfo.create() : RxNetworkInfo.createFrom(networkInfo);
    }

    /**
     * Gets {@link RxNetworkInfo network information} from given {@link Network} instance
     * along with {@link NetworkCapabilities} provided at the time of registering network callback
     * in {@linkplain ConnectivityManager#registerNetworkCallback} (if available).
     *
     * @param network             {@link Network}
     * @param connectivityManager {@link ConnectivityManager}
     *
     * @return {@link RxNetworkInfo} instance
     */
    @RequiresApi(LOLLIPOP)
    public static RxNetworkInfo getRxNetworkInfoFrom(@NonNull Network network,
            @NonNull ConnectivityManager connectivityManager) {

        checkNotNull(network, "network");
        checkNotNull(connectivityManager, "manager");

        final NetworkInfo networkInfo = getNetworkInfo(network, connectivityManager);
        final NetworkCapabilities networkCapabilities
                = getNetworkCapabilities(network, connectivityManager);

        return networkInfo != null ? RxNetworkInfo.builderFrom(networkInfo)
                .networkCapabilities(networkCapabilities).build() : RxNetworkInfo.create();
    }

    @RequiresApi(LOLLIPOP)
    @Nullable
    private static NetworkCapabilities getNetworkCapabilities(@NonNull Network network,
            @NonNull ConnectivityManager connectivityManager) {

        NetworkCapabilities networkCapabilities = null;

        try {
            networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        } catch (Exception exc) {
            logger.log(WARNING, "Could not retrieve network capabilities from provided network: "
                    + exc.getMessage());
        }

        return networkCapabilities;
    }

    @RequiresApi(LOLLIPOP)
    @Nullable
    private static NetworkInfo getNetworkInfo(@NonNull Network network,
            @NonNull ConnectivityManager connectivityManager) {

        NetworkInfo networkInfo = null;

        try {
            networkInfo = connectivityManager.getNetworkInfo(network);
        } catch (Exception exc) {
            logger.log(WARNING, "Could not retrieve network info from provided network: "
                    + exc.getMessage());
        }

        return networkInfo;
    }
}
