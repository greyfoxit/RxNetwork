package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Radek Kozak
 */

public abstract class BaseEndpointInternetObservingStrategy implements InternetObservingStrategy {

    abstract long delay();

    abstract long interval();

    abstract Logger logger();

    Function<Long, Boolean> toConnectionState() {
        return new Function<Long, Boolean>() {
            @Override
            public Boolean apply(Long tick) throws Exception {
                return checkConnection();
            }
        };
    }

    @Override
    public Observable<Boolean> observe() {
        return Observable.interval(delay(), interval(), TimeUnit.MILLISECONDS)
                .map(toConnectionState()).distinctUntilChanged();
    }

    protected abstract boolean checkConnection();

    void onError(String message, Exception exception) {
        logger().log(Level.WARNING, message + ": " + exception.getMessage()
                + ((exception.getCause() != null) ? ": " + exception.getCause().getMessage() : ""));
    }

    /**
     * {@code BuiltInInternetObservingStrategy} builder static inner class.
     */
    public static abstract class Builder<T extends Builder> {

        static final int DEFAULT_DELAY_MS = 0;
        static final int DEFAULT_INTERVAL_MS = 3000;
        static final String DEFAULT_ENDPOINT = "www.google.cn";
        static final int DEFAULT_PORT = 80;

        private long delay = DEFAULT_DELAY_MS;
        private long interval = DEFAULT_INTERVAL_MS;
        private String endpoint = DEFAULT_ENDPOINT;
        private int port = DEFAULT_PORT;

        public long delay() {
            return this.delay;
        }

        public long interval() {
            return this.interval;
        }

        public String endpoint() {
            return this.endpoint;
        }

        public int port() {
            return this.port;
        }

        /**
         * Sets the {@code delay} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param delay the {@code delay} to set
         *
         * @return a reference to this Builder
         */
        @NonNull
        public T delay(long delay) {
            this.delay = delay;
            return self();
        }

        /**
         * Sets the {@code interval} and returns a reference to this Builder so that the methods can
         * be chained together.
         *
         * @param interval the {@code interval} to set
         *
         * @return a reference to this Builder
         */
        @NonNull
        public T interval(long interval) {
            this.interval = interval;
            return self();
        }

        /**
         * Sets the {@code endpoint} and returns a reference to this Builder so that the methods can
         * be chained together.
         *
         * @param endpoint the {@code endpoint} to set
         *
         * @return a reference to this Builder
         */
        @NonNull
        public T endpoint(@NonNull String endpoint) {
            this.endpoint = checkNotNull(endpoint, "endpoint");
            return self();
        }

        /**
         * Sets the {@code port} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param port the {@code port} to set
         *
         * @return a reference to this Builder
         */
        @NonNull
        public T port(int port) {
            if (port <= 0 || port > 65535)
                throw new IllegalArgumentException("Invalid port: " + port);

            this.port = port;
            return self();
        }

        /**
         * Returns a {@code InternetObservingStrategy} built from the parameters previously
         * set.
         *
         * @return a {@code InternetObservingStrategy} built with parameters of
         * this {@code Builder}
         */
        @NonNull
        public abstract InternetObservingStrategy build();

        protected final T self() {
            //noinspection unchecked
            return (T) this;
        }
    }
}
