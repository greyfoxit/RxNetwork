package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Radek Kozak
 */

public class SocketInternetObservingStrategy extends BaseEndpointInternetObservingStrategy {

    private static final Logger logger
            = getLogger(SocketInternetObservingStrategy.class.getSimpleName());

    /** Canonical hostname. */
    @NonNull private final String host;

    /** Either default 80 or a user-specified port. In range [1..65535]. */
    private final int port;

    private final long delay;
    private final int timeout;
    private final long interval;

    @VisibleForTesting(otherwise = PRIVATE)
    SocketInternetObservingStrategy() {
        throw new AssertionError("Use static factory methods or Builder to create strategy");
    }

    @VisibleForTesting(otherwise = PRIVATE)
    @RestrictTo(LIBRARY_GROUP)
    private SocketInternetObservingStrategy(@NonNull Builder builder) {
        checkNotNull(builder, "builder == null");

        delay = builder.delay();
        timeout = builder.timeout();
        interval = builder.interval();
        host = builder.endpoint();
        port = builder.port();
    }

    @NonNull
    public static InternetObservingStrategy create() {
        return builder().build();
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

/*    @Override
    public Observable<Boolean> observe() {
        return Observable.interval(delay, interval, TimeUnit.MILLISECONDS)
                .map(toConnectionState()).distinctUntilChanged();
    }*/

    @Override
    long delay() {
        return this.delay;
    }

    @Override
    long interval() {
        return this.interval;
    }

    @Override
    protected boolean checkConnection() {
        boolean isConnected;
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            isConnected = socket.isConnected();
        } catch (IOException ioe) {
            logger.log(WARNING, "Problem occurred while checking endpoint: " + ioe.getMessage());
            isConnected = Boolean.FALSE;
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException ioe) {
                logger.log(Level.WARNING, "Could not close the socket: " + ioe.getMessage());
            }
        }
        return isConnected;
    }

    /**
     * {@code SocketInternetObservingStrategy} builder static inner class.
     */
    public static final class Builder extends
            BaseEndpointInternetObservingStrategy.Builder<SocketInternetObservingStrategy.Builder> {

        private int timeout = Config.DEFAULT_TIMEOUT_MS;

        public int timeout() {
            return this.timeout;
        }

        /**
         * Sets the {@code timeout} and returns a reference to this Builder so that the methods can
         * be chained together.
         *
         * @param timeout the {@code timeout} to set
         *
         * @return a reference to this Builder
         */
        @NonNull
        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Returns a {@code SocketInternetObservingStrategy} built from the parameters
         * previously set.
         *
         * @return a {@code SocketInternetObservingStrategy} built with parameters
         * of this {@code SocketInternetObservingStrategy.Builder}
         */
        @NonNull
        @Override
        public InternetObservingStrategy build() {
            return new SocketInternetObservingStrategy(this);
        }
    }

    public static final class Config {

        static final int DEFAULT_TIMEOUT_MS = 3000;
    }
}
