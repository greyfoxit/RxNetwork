package greyfox.rxnetwork2.internal.strategy.network.impl;

import greyfox.rxnetwork2.internal.strategy.network.NetworkObservingStrategy;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for network observing strategies.
 *
 * @author Radek Kozak
 */
public abstract class BaseNetworkObservingStrategy implements NetworkObservingStrategy {

    abstract void dispose();

    abstract Logger logger();

    void onError(String message, Exception exception) {
        logger().log(Level.WARNING, message + ": " + exception.getMessage());
    }

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
