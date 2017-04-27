package greyfox.rxnetwork.internal.strategy.network.providers;

import static android.support.annotation.VisibleForTesting.PRIVATE;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArraySet;
import greyfox.rxnetwork.internal.strategy.ObservingStrategyProviders;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategyProvider;
import java.util.Collection;
import java.util.Collections;

/**
 * RxNetwork's built-in providers of network observing strategies.
 *
 * @author Radek Kozak
 */
public final class BuiltInNetworkObservingStrategyProviders implements
        ObservingStrategyProviders<NetworkObservingStrategyProvider> {

    private final Context context;

    @VisibleForTesting(otherwise = PRIVATE)
    BuiltInNetworkObservingStrategyProviders() {
        throw new AssertionError("No instances.");
    }

    public BuiltInNetworkObservingStrategyProviders(@NonNull Context context) {
        this.context = checkNotNull(context, "context == null");
    }

    /**
     * Gets collection of unmodifiable {@link ObservingStrategyProviders}'s.
     *
     * @return Collection of {@linkplain NetworkObservingStrategyProvider providers}
     */
    @Override
    public Collection<NetworkObservingStrategyProvider> get() {
        Collection<NetworkObservingStrategyProvider> collection = new ArraySet<>();

        collection.add(new PreLollipopNetworkObservingStrategyProvider(context));
        collection.add(new LollipopNetworkObservingStrategyProvider(context));
        collection.add(new MarshmallowNetworkObservingStrategyProvider(context));

        return Collections.unmodifiableCollection(collection);
    }
}
