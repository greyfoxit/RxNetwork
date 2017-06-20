package greyfox.rxnetwork.internal.strategy.network.providers;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.NetworkRequest;
import android.support.annotation.NonNull;
import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategyProvider;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;

/**
 * Base provider for Lollipop+ (<i>API > 21</i>) network observing strategies.
 *
 * @author Radek Kozak
 */
@TargetApi(LOLLIPOP)
abstract class Api21NetworkObservingStrategyProvider implements NetworkObservingStrategyProvider {

  protected final Context context;
  protected NetworkRequest networkRequest;

  Api21NetworkObservingStrategyProvider(@NonNull Context context) {
    this.context = checkNotNull(context, "context");
  }

  Api21NetworkObservingStrategyProvider(@NonNull Context context,
      @NonNull NetworkRequest networkRequest) {

    this(context);
    this.networkRequest = checkNotNull(networkRequest, "networkRequest");
  }
}
