package greyfox.rxnetwork2.internal.strategy.network.impl;

import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategy;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;

/**
 * Base class for network observing strategies.
 *
 * @author Radek Kozak
 */
public abstract class BuiltInNetworkObservingStrategy implements NetworkObservingStrategy {

    abstract void dispose();

    final class OnDisposeAction implements Action {

        @Override
        public void run() throws Exception {
            dispose();
        }
    }

    final class StrategyCancellable implements Cancellable {

        @Override
        public void cancel() throws Exception {
            dispose();
        }
    }
}
