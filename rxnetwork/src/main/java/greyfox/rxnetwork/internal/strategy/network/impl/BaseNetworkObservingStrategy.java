/*
 * Copyright (C) 2017 Greyfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greyfox.rxnetwork.internal.strategy.network.impl;

import greyfox.rxnetwork.internal.strategy.network.NetworkObservingStrategy;
import io.reactivex.functions.Action;
import io.reactivex.functions.Cancellable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for network observing strategies.
 *
 * @author Radek Kozak
 */
abstract class BaseNetworkObservingStrategy implements NetworkObservingStrategy {

  abstract void dispose();

  abstract Logger logger();

  void onError(String message, Exception exception) {
    logger().log(Level.WARNING, message + ": " + exception.getMessage());
  }

  final class OnDisposeAction implements Action {

    @Override
    public void run() throws Exception {
      dispose();
    }
  }

  final class StrategyCancellable implements Cancellable {

    @Override
    public void cancel() throws Exception {
      dispose();
    }
  }
}
