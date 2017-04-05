package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static java.util.logging.Logger.getLogger;

import static greyfox.rxnetwork2.internal.strategy.internet.impl.Http200InternetObservingStrategy.Config.DEFAULT_ENDPOINT;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Logger;

/**
 * @author Radek Kozak
 */

public class Http200InternetObservingStrategy extends BuiltInInternetObservingStrategy {

    private static final Logger logger
            = getLogger(Http200InternetObservingStrategy.class.getSimpleName());

    @VisibleForTesting(otherwise = PRIVATE)
    @RestrictTo(LIBRARY_GROUP)
    private Http200InternetObservingStrategy(@NonNull Builder builder) {
        super(builder);
    }

    @NonNull
    public static InternetObservingStrategy create() {
        return builder().build();
    }

    @NonNull
    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected boolean isConnected(@NonNull HttpURLConnection urlConnection)
            throws InternetObservingStrategyException {

        try {
            return urlConnection.getResponseCode() == 200;
        } catch (IOException ioe) {
            throw new InternetObservingStrategyException("Unable to check internet access", ioe);
        }
    }

    /**
     * {@code Http200InternetObservingStrategy} builder static inner class.
     */
    public static final class Builder
            extends BuiltInInternetObservingStrategy.Builder {

        public Builder() {
            this.endpoint(DEFAULT_ENDPOINT);
        }

        /**
         * Returns a {@code Http200InternetObservingStrategy} built from the parameters
         * previously set.
         *
         * @return a {@code Http200InternetObservingStrategy} built with parameters
         * of this {@code Http200InternetObservingStrategy.Builder}
         */
        @NonNull
        @Override
        public InternetObservingStrategy build() {
            return new Http200InternetObservingStrategy(this);
        }
    }

    public static final class Config {

        static final String DEFAULT_ENDPOINT = "http://www.google.cn/blank.html";
    }
}
