package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;
import static greyfox.rxnetwork2.internal.strategy.internet.impl.BuiltInInternetObservingStrategy.Config.DEFAULT_ENDPOINT;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Radek Kozak
 */
public class BuiltInInternetObservingStrategy extends BaseEndpointInternetObservingStrategy {

    private static final Logger logger
            = getLogger(BuiltInInternetObservingStrategy.class.getSimpleName());

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

        delay = builder.delay();
        timeout = builder.timeout();
        interval = builder.interval();
        endpoint = builder.endpoint();
        port = builder.port();
    }

    @NonNull
    public static InternetObservingStrategy create() {
        return builder().build();
    }

    @NonNull
    public static Builder builder() { return new Builder(); }

    private HttpURLConnection buildConnection(@NonNull URL url)
            throws InternetObservingStrategyException {

        checkNotNull(url, "url");

        final HttpURLConnection urlConnection;

        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(timeout);
            urlConnection.setReadTimeout(timeout);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
        } catch (IOException ioe) {
            throw new InternetObservingStrategyException("Could not create valid connection " +
                    "from " + url.toString(), ioe);
        }

        return urlConnection;
    }

    protected boolean isConnected(@NonNull HttpURLConnection urlConnection)
            throws InternetObservingStrategyException {

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
    protected boolean checkConnection() {
        HttpURLConnection urlConnection = null;
        final URL url;
        try {
            urlConnection = buildConnection(url());
            return isConnected(urlConnection);
        } catch (InternetObservingStrategyException iose) {
            logger.log(WARNING, "Problem occurred while checking endpoint: " + iose.getMessage()
                    + " : " + iose.getCause().getMessage());
            return false;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    /*@Override
    public Observable<Boolean> observe() {
        return Observable.interval(delay, interval, TimeUnit.MILLISECONDS)
                .map(toConnectionState()).distinctUntilChanged();
    }*/

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
            BaseEndpointInternetObservingStrategy.Builder<BuiltInInternetObservingStrategy.Builder> {

        private int timeout = SocketInternetObservingStrategy.Config.DEFAULT_TIMEOUT_MS;

        public Builder() {
            this.endpoint(DEFAULT_ENDPOINT);
        }

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
        public BaseEndpointInternetObservingStrategy.Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
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
        public InternetObservingStrategy build() {
            return new BuiltInInternetObservingStrategy(this);
        }
    }

    public static final class Config {

        static final String DEFAULT_ENDPOINT = "http://g.cn/generate_204";
    }
}
