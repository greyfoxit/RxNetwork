package greyfox.rxnetwork2.internal.strategy.internet.impl;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static java.util.logging.Logger.getLogger;

import static greyfox.rxnetwork2.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import greyfox.rxnetwork2.internal.strategy.internet.InternetObservingStrategy;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Radek Kozak
 */

public class PingInternetObservingStrategy extends BaseEndpointInternetObservingStrategy {

    private static final Logger logger
            = getLogger(PingInternetObservingStrategy.class.getSimpleName());

    /** Canonical hostname. */
    @NonNull private final String endpoint;

    private final long delay;
    private final long interval;

    @VisibleForTesting(otherwise = PRIVATE)
    @RestrictTo(LIBRARY_GROUP)
    private PingInternetObservingStrategy(@NonNull Builder builder) {
        checkNotNull(builder, "builder == null");

        delay = builder.delay();
        interval = builder.interval();
        endpoint = builder.endpoint();
    }

    @NonNull
    public static InternetObservingStrategy create() {
        return builder().build();
    }

    @NonNull
    public static Builder builder() { return new Builder(); }

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
        Runtime runtime = Runtime.getRuntime();
        try {
            final String ping = "/system/bin/ping -c 1 " + endpoint;
            Process ipProcess = runtime.exec(ping);
            int exitValue = ipProcess.waitFor();
            logger.log(Level.INFO, "Exit value: " + exitValue);
            return (exitValue == 0);
        } catch (IOException | InterruptedException exc) {
            logger.log(Level.INFO, "Problem pinging: " + exc.getMessage());
        }

        return false;
    }

    /**
     * {@code PingInternetObservingStrategy} builder static inner class.
     */
    public static class Builder extends
            BaseEndpointInternetObservingStrategy.Builder<PingInternetObservingStrategy.Builder> {

        @NonNull
        @Override
        public InternetObservingStrategy build() {
            return new PingInternetObservingStrategy(this);
        }
    }
}
