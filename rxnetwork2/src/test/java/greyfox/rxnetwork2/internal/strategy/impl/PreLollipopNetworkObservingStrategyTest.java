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
package greyfox.rxnetwork2.internal.strategy.impl;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Context;
import greyfox.rxnetwork2.internal.net.RxNetworkInfo;
import greyfox.rxnetwork2.internal.strategy.network.impl.BuiltInNetworkObservingStrategy;
import greyfox.rxnetwork2.internal.strategy.network.impl.PreLollipopNetworkObservingStrategy;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
@RunWith(MockitoJUnitRunner.class)
public class PreLollipopNetworkObservingStrategyTest {

    @Mock Context context;

    BuiltInNetworkObservingStrategy sut;
    TestObserver<RxNetworkInfo> testObserver = new TestObserver<>();

    @Before
    public void setUp() {
        sut = spy(new PreLollipopNetworkObservingStrategy(context));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrow_whenTryingToInstantiateWithoutContext() {
        new PreLollipopNetworkObservingStrategy(null);
    }

    @Test
    public void shouldSubscribeCorrectly() {
        sut.observe().subscribeWith(testObserver);

        testObserver.assertSubscribed().assertEmpty();
    }

    @Test
    public void shouldDisposeCorrectly_whenDisposed() {
        sut.observe().subscribeWith(testObserver).assertSubscribed();

        testObserver.dispose();

        verify(sut).dispose();
        testObserver.isDisposed();
    }
}
