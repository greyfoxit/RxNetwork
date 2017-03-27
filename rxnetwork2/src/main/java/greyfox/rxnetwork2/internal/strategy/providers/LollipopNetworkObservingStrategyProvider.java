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
package greyfox.rxnetwork2.internal.strategy.providers;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;
import static greyfox.rxnetwork2.internal.os.Build.isAtLeastLollipop;
import static greyfox.rxnetwork2.internal.os.Build.isLessThanMarshmallow;

import android.content.Context;
import android.support.annotation.NonNull;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategyProvider;
import greyfox.rxnetwork2.internal.strategy.impl.LollipopNetworkObservingStrategy;

/**
 * Provides network observing strategy implementation for Lollipop devices.
 *
 * @author Radek Kozak
 */
final class LollipopNetworkObservingStrategyProvider
        implements NetworkObservingStrategyProvider {

    private final Context context;

    LollipopNetworkObservingStrategyProvider(@NonNull Context context) {
        this.context = checkNotNull(context, "context == null");
    }

    @Override
    public boolean canProvide() {
        return isAtLeastLollipop() && isLessThanMarshmallow();
    }

    @Override
    public LollipopNetworkObservingStrategy provide() {
        return new LollipopNetworkObservingStrategy(context);
    }
}
