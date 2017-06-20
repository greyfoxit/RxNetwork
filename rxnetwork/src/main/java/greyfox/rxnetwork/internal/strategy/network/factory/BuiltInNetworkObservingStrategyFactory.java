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
package greyfox.rxnetwork.internal.strategy.network.factory;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategyFactory;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategyProvider;
import java.util.Collection;

import static android.os.Build.VERSION.CODENAME;
import static android.os.Build.VERSION.SDK_INT;
import static android.support.annotation.VisibleForTesting.PRIVATE;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * Library's built-in implementation of {@link NetworkObservingStrategyFactory}.
 * <p>
 * This factory provides API-specific implementation of concrete {@link NetworkObservingStrategy}
 *
 * @author Radek Kozak
 */
public final class BuiltInNetworkObservingStrategyFactory
    implements NetworkObservingStrategyFactory {

  private final Collection<NetworkObservingStrategyProvider> strategyProviders;

  @VisibleForTesting(otherwise = PRIVATE)
  BuiltInNetworkObservingStrategyFactory(
      @NonNull Collection<NetworkObservingStrategyProvider> strategyProviders) {
    this.strategyProviders = checkNotNull(strategyProviders, "strategyProviders");
  }

  public static NetworkObservingStrategyFactory create(
      @NonNull Collection<NetworkObservingStrategyProvider> providers) {
    return new BuiltInNetworkObservingStrategyFactory(providers);
  }

  @NonNull
  @Override
  public NetworkObservingStrategy get() {
    for (NetworkObservingStrategyProvider strategyProvider : strategyProviders) {
      if (strategyProvider.canProvide()) {
        return strategyProvider.provide();
      }
    }
    throw new NullPointerException(
        "No NetworkObservingStrategy found for API level " + SDK_INT + " ( " + CODENAME + " )");
  }
}

