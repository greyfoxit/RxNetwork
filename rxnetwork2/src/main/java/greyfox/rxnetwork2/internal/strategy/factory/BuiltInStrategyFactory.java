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
package greyfox.rxnetwork2.internal.strategy.factory;

import static android.os.Build.VERSION.CODENAME;
import static android.os.Build.VERSION.SDK_INT;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork2.RxNetwork;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategyFactory;
import greyfox.rxnetwork2.internal.strategy.NetworkObservingStrategyProvider;
import java.util.Collection;

/**
 * {@link RxNetwork}'s default implementation of {@link NetworkObservingStrategyFactory}
 * providing api-level dependent implementation of concrete network observing
 * {@link NetworkObservingStrategy strategy}.
 *
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
public final class BuiltInStrategyFactory
        implements NetworkObservingStrategyFactory<NetworkObservingStrategy> {

    private final Collection<NetworkObservingStrategyProvider> strategyProviders;

    @VisibleForTesting(otherwise = PRIVATE)
    BuiltInStrategyFactory(
            @NonNull Collection<NetworkObservingStrategyProvider> strategyProviders) {
        this.strategyProviders = checkNotNull(strategyProviders, "strategyProviders == null");
    }

    public static NetworkObservingStrategyFactory create(
            Collection<NetworkObservingStrategyProvider> providers) {
        return new BuiltInStrategyFactory(providers);
    }

    @NonNull
    @Override
    public NetworkObservingStrategy get() {
        for (NetworkObservingStrategyProvider strategyProvider : strategyProviders) {
            if (strategyProvider.canProvide()) return strategyProvider.provide();
        }
        throw new NullPointerException("No NetworkObservingStrategy found for API level "
                + SDK_INT + " ( " + CODENAME + " )");
    }
}

