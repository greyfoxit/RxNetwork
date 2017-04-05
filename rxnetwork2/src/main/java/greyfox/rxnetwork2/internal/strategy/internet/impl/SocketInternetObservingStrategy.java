package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static java.util.logging.Logger.getLogger;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Logger;

/**
 * @author Radek Kozak
 */

public class SocketInternetObservingStrategy extends BaseEndpointInternetObservingStrategy {

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
    SocketInternetObservingStrategy(@NonNull Builder builder) {
        checkNotNull(builder, "builder");

        delay = builder.delay();
        timeout = builder.timeout();
        interval = builder.interval();
        host = builder.endpoint();
        port = builder.port();
    }

    @NonNull
    public static SocketInternetObservingStrategy create() {
        return builder().build();
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    @Override
    long delay() {
        return this.delay;
    }

    @Override
    long interval() {
        return this.interval;
    }

    @Override
    Logger logger() {
        return getLogger(SocketInternetObservingStrategy.class.getSimpleName());
    }

    @Override
    protected boolean checkConnection() {
        boolean isConnected;
        Socket socket = null;
        try {
            socket = connectSocketTo(new InetSocketAddress(host, port), timeout);
            isConnected = isSocketConnected(socket);
        } catch (IOException ioe) {
            onError("Problem occurred while checking endpoint", ioe);
            isConnected = Boolean.FALSE;
        } finally {
            try {
                if (socket != null) socket.close();
            } catch (IOException ioe) {
                onError("Could not close the socket", ioe);
            }
        }

        return isConnected;
    }

    Socket connectSocketTo(SocketAddress socketAddress, int timeout) throws IOException {
        final Socket socket = new Socket();
        socket.connect(socketAddress, timeout);
        return socket;
    }

    boolean isSocketConnected(Socket socket) {
        return socket.isConnected();
    }

    /**
     * {@code SocketInternetObservingStrategy} builder static inner class.
     */
    public static final class Builder extends BaseEndpointInternetObservingStrategy
            .Builder<SocketInternetObservingStrategy.Builder> {

        private static final int DEFAULT_TIMEOUT_MS = 3000;

        private int timeout = DEFAULT_TIMEOUT_MS;

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
            return self();
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
        public SocketInternetObservingStrategy build() {
            return new SocketInternetObservingStrategy(this);
        }
    }
}
