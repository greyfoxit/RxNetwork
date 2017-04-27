package greyfox.rxnetwork.internal.strategy.internet.impl;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

import static java.util.logging.Logger.getLogger;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Radek Kozak
 */
public class BuiltInInternetObservingStrategy extends BaseEndpointInternetObservingStrategy {

    /** Canonical hostname. */
    @NonNull private final String endpoint;

    private final int port;
    private final long delay;
    private final int timeout;
    private final long interval;

    @VisibleForTesting(otherwise = PRIVATE)
    BuiltInInternetObservingStrategy() {
        throw new AssertionError("Use static factory methods or Builder to create strategy");
    }

    @VisibleForTesting(otherwise = PRIVATE)
    @RestrictTo(LIBRARY_GROUP)
    BuiltInInternetObservingStrategy(@NonNull Builder builder) {
        checkNotNull(builder, "builder");

        delay = builder.getDelay();
        timeout = builder.getTimeout();
        interval = builder.getInterval();
        endpoint = builder.getEndpoint();
        port = builder.getPort();
    }

    @NonNull
    public static BuiltInInternetObservingStrategy create() {
        return builder().build();
    }

    @NonNull
    public static Builder builder() { return new Builder(); }

    @VisibleForTesting(otherwise = PRIVATE)
    HttpURLConnection buildUrlConnection(@NonNull URL url)
            throws InternetObservingStrategyException {

        checkNotNull(url, "url");

        final HttpURLConnection urlConnection;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            setUpUrlConnectionForStrategy(urlConnection);
        } catch (IOException ioe) {
            throw new InternetObservingStrategyException("Could not create valid connection "
                    + "from " + url.toString(), ioe);
        }

        return urlConnection;
    }

    private void setUpUrlConnectionForStrategy(@NonNull HttpURLConnection urlConnection)
            throws IOException {

        checkNotNull(urlConnection, "urlConnection");

        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setReadTimeout(timeout);
        urlConnection.setUseCaches(false);
        urlConnection.getInputStream();
    }

    public boolean isConnected(@NonNull HttpURLConnection urlConnection)
            throws InternetObservingStrategyException {

        checkNotNull(urlConnection, "urlConnection");

        try {
            return urlConnection.getResponseCode() == 204;
        } catch (IOException ioe) {
            throw new InternetObservingStrategyException("Unable to check internet access", ioe);
        }
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
        return getLogger(BuiltInInternetObservingStrategy.class.getSimpleName());
    }

    @Override
    protected boolean checkConnection() {
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = buildUrlConnection(url());
            return isConnected(urlConnection);
        } catch (InternetObservingStrategyException iose) {
            onError("Problem occurred while checking endpoint", iose);
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /** Returns this URL as a {@link URL java.net.URL}. */
    public URL url() throws InternetObservingStrategyException {
        try {
            return new URL(endpoint);
        } catch (MalformedURLException mue) {
            throw new InternetObservingStrategyException("Couldn't create valid endpoint", mue);
        }
    }

    /**
     * {@code BuiltInInternetObservingStrategy} builder static inner class.
     */
    public static class Builder extends
            BaseEndpointInternetObservingStrategy.Builder<Builder> {

        private static final String DEFAULT_ENDPOINT = "http://g.cn/generate_204";
        private static final int DEFAULT_TIMEOUT_MS = 3000;

        private int timeout = DEFAULT_TIMEOUT_MS;

        public Builder() {
            this.endpoint(DEFAULT_ENDPOINT);
        }

        public int getTimeout() {
            return timeout;
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
         * Returns a {@code BuiltInInternetObservingStrategy} built from the parameters previously
         * set.
         *
         * @return a {@code BuiltInInternetObservingStrategy} built with parameters of this {@code
         * BuiltInInternetObservingStrategy.Builder}
         */
        @NonNull
        @Override
        public BuiltInInternetObservingStrategy build() {
            return new BuiltInInternetObservingStrategy(this);
        }
    }
}
