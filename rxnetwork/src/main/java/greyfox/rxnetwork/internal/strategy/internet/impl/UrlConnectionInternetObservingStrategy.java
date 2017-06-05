/*
 * Copyright (C) 2017 Greyfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greyfox.rxnetwork.internal.strategy.internet.impl;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork.internal.strategy.internet.error.InternetObservingStrategyException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Radek Kozak
 */

public abstract class UrlConnectionInternetObservingStrategy
        extends EndpointInternetObservingStrategy {

    UrlConnectionInternetObservingStrategy() {}

    UrlConnectionInternetObservingStrategy(@NonNull Builder builder) {
        super(builder);
    }

    @VisibleForTesting()
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

    /**
     * This is taken straight from Android's inner works
     * ({@code android.net.wifi.WifiWatchdogStateMachine}) and what essentially
     * makes the strategy handle walled-garden internet situations.
     */
    private void setUpUrlConnectionForStrategy(@NonNull HttpURLConnection urlConnection)
            throws IOException {

        checkNotNull(urlConnection, "urlConnection");

        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setReadTimeout(timeout);
        urlConnection.setUseCaches(false);
        urlConnection.getInputStream();
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
    private URL url() throws InternetObservingStrategyException {
        try {
            return new URL(endpoint);
        } catch (MalformedURLException mue) {
            throw new InternetObservingStrategyException("Couldn't create valid endpoint", mue);
        }
    }

    abstract boolean isConnected(@NonNull HttpURLConnection urlConnection)
            throws InternetObservingStrategyException;

    /**
     * {@code EndpointInternetObservingStrategy} builder static inner class.
     */
    public abstract static class Builder<S extends UrlConnectionInternetObservingStrategy,
            B extends UrlConnectionInternetObservingStrategy.Builder<S, B>>
            extends EndpointInternetObservingStrategy.Builder<S, B> {
    }
}
