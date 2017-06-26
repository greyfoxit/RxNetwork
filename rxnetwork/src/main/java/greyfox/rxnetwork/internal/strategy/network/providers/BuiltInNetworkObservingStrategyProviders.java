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

import android.content.Context;
import android.net.NetworkRequest;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * Library's built-in providers of network observing strategies.
 *
 * @author Radek Kozak
 */
@RestrictTo(LIBRARY_GROUP)
public final class BuiltInNetworkObservingStrategyProviders
    implements ObservingStrategyProviders<NetworkObservingStrategyProvider> {

  private final Context context;
  private NetworkRequest networkRequest;

  @VisibleForTesting
  BuiltInNetworkObservingStrategyProviders() {
    throw new AssertionError("No instances.");
  }

  public BuiltInNetworkObservingStrategyProviders(@NonNull Context context) {
    this.context = checkNotNull(context, "context == null");
  }

  public BuiltInNetworkObservingStrategyProviders(@NonNull Context context,
      @NonNull NetworkRequest networkRequest) {
    this(context);
    this.networkRequest = checkNotNull(networkRequest, "networkRequest");
  }

  /**
   * Gets collection of unmodifiable {@link NetworkObservingStrategyProvider}'s.
   *
   * @return Collection of {@linkplain NetworkObservingStrategyProvider providers}
   */
  @Override
  public Collection<NetworkObservingStrategyProvider> get() {
    Collection<NetworkObservingStrategyProvider> collection = new HashSet<>();

    collection.add(new PreLollipopNetworkObservingStrategyProvider(context));

    collection.add(networkRequest == null ? new LollipopNetworkObservingStrategyProvider(context)
                                          : new LollipopNetworkObservingStrategyProvider(context,
                                              networkRequest));

    collection.add(networkRequest == null ? new MarshmallowNetworkObservingStrategyProvider(context)
                                          : new MarshmallowNetworkObservingStrategyProvider(context,
                                              networkRequest));

    return Collections.unmodifiableCollection(collection);
  }
}
