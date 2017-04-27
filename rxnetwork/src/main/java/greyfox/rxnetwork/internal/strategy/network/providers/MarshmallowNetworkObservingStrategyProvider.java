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
package greyfox.rxnetwork.internal.strategy.network.providers;

import static android.os.Build.VERSION_CODES.M;

import static greyfox.rxnetwork.internal.os.Build.isAtLeastMarshmallow;

import android.content.Context;
import android.net.NetworkRequest;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork.internal.strategy.network.impl.MarshmallowNetworkObservingStrategy;

/**
 * Provides network observing strategy implementation for Marshmallow devices.
 *
 * @author Radek Kozak
 */
final class MarshmallowNetworkObservingStrategyProvider
        extends Api21NetworkObservingStrategyProvider {

    MarshmallowNetworkObservingStrategyProvider(@NonNull Context context) {
        super(context);
    }

    MarshmallowNetworkObservingStrategyProvider(@NonNull Context context,
            @NonNull NetworkRequest networkRequest) {

        super(context, networkRequest);
    }

    @Override
    public boolean canProvide() {
        return isAtLeastMarshmallow();
    }

    @Override
    @RequiresApi(M)
    public NetworkObservingStrategy provide() {
        return networkRequest == null ? new MarshmallowNetworkObservingStrategy(context)
                : new MarshmallowNetworkObservingStrategy(context, networkRequest);
    }
}
