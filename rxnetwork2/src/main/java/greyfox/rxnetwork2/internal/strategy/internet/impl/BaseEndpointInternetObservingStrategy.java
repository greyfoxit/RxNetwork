package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;

/**
 * @author Radek Kozak
 */

public abstract class BaseEndpointInternetObservingStrategy implements InternetObservingStrategy {

    abstract long delay();

    abstract long interval();

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

    /**
     * {@code BuiltInInternetObservingStrategy} builder static inner class.
     */
    public static abstract class Builder<T extends Builder> {

        private long delay = Config.DEFAULT_DELAY_MS;
        //private int timeout = Config.DEFAULT_TIMEOUT_MS;
        private long interval = Config.DEFAULT_INTERVAL_MS;
        private String endpoint = Config.DEFAULT_ENDPOINT;
        private int port = Config.DEFAULT_PORT;

        public long delay() {
            return this.delay;
        }

        /*public int timeout() {
            return this.timeout;
        }*/

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
         * Sets the {@code timeout} and returns a reference to this Builder so that the methods can
         * be chained together.
         *
         * @param timeout the {@code timeout} to set
         *
         * @return a reference to this Builder
         */
        /*@NonNull
        public T timeout(int timeout) {
            this.timeout = timeout;
            return self();
        }*/

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
            this.endpoint = checkNotNull(endpoint, "endpoint == null");
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

    public static final class Config {

        static final int DEFAULT_DELAY_MS = 0;
        static final int DEFAULT_INTERVAL_MS = 3000;
        //static final int DEFAULT_TIMEOUT_MS = 3000;
        static final String DEFAULT_ENDPOINT = "www.google.cn";
        static final int DEFAULT_PORT = 80;
    }
}
