package greyfox.rxnetwork2.internal.strategy.network.providers;

import static android.os.Build.VERSION_CODES.M;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;
import static greyfox.rxnetwork2.internal.os.Build.isAtLeastMarshmallow;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategyProvider;
import greyfox.rxnetwork2.internal.strategy.network.impl.MarshmallowNetworkObservingStrategy;

/**
 * Provides network observing strategy implementation for Marshmallow devices.
 *
 * @author Radek Kozak
 */
final class MarshmallowNetworkObservingStrategyProvider
        implements NetworkObservingStrategyProvider {

    private final Context context;

    MarshmallowNetworkObservingStrategyProvider(@NonNull Context context) {
        this.context = checkNotNull(context, "context");
    }

    @Override
    public boolean canProvide() {
        return isAtLeastMarshmallow();
    }

    @Override
    @RequiresApi(M)
    public NetworkObservingStrategy provide() {
        return new MarshmallowNetworkObservingStrategy(context);
    }
}
