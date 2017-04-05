package greyfox.rxnetwork2.internal.strategy.network.providers;

import static android.support.annotation.VisibleForTesting.PRIVATE;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArraySet;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategyProvider;
import java.util.Collection;
import java.util.Collections;

/**
 * RxNetwork's built-in providers of network observing strategies.
 *
 * @author Radek Kozak
 */
public final class BuiltInNetworkObservingStrategyProviders {

    @VisibleForTesting(otherwise = PRIVATE)
    BuiltInNetworkObservingStrategyProviders() {
        throw new AssertionError("No instances");
    }

    /**
     * Gets collection of unmodifiable {@link NetworkObservingStrategyProvider}'s.
     *
     * @param context {@link Context}
     *
     * @return Collection of {@linkplain NetworkObservingStrategyProvider providers}
     */
    public static Collection<NetworkObservingStrategyProvider> get(@NonNull Context context) {
        checkNotNull(context, "context");

        Collection<NetworkObservingStrategyProvider> collection = new ArraySet<>();
        collection.add(new PreLollipopNetworkObservingStrategyProvider(context));
        collection.add(new LollipopNetworkObservingStrategyProvider(context));
        collection.add(new MarshmallowNetworkObservingStrategyProvider(context));

        return Collections.unmodifiableCollection(collection);
    }
}
