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
package greyfox.rxnetwork.internal.strategy.network;

import greyfox.rxnetwork.internal.net.RxNetworkInfo;
import greyfox.rxnetwork.internal.strategy.ObservingStrategy;

/**
 * Interface allowing to implement different strategies for monitoring network connectivity change.
 * (Network monitoring API may differ depending of specific Android version)
 *
 * @author Radek Kozak
 */
public interface NetworkObservingStrategy extends ObservingStrategy<RxNetworkInfo> {

}
