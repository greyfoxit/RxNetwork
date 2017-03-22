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
package greyfox.rxnetwork2.common.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.junit.Test;

public class PreconditionsTest {

    private static final String NON_NULL_STRING = "non null string";
    private static final String ERROR_MESSAGE = "error message";

    @Test(expected = AssertionError.class)
    public void shouldThrow_whenTryingToInstantiateViaConstructor() {
        new Preconditions();
    }

    @Test
    public void checkNotNull_shouldBeValid() {
        String result = Preconditions.checkNotNull(NON_NULL_STRING);
        assertThat(NON_NULL_STRING).isEqualTo(result);
    }

    @Test(expected = NullPointerException.class)
    public void checkNotNull_shouldThrow() {
        Preconditions.checkNotNull(null);
    }

    @Test
    public void checkNotNullWithErrorMessage_shouldBeValid() {
        String result = Preconditions.checkNotNull(NON_NULL_STRING, ERROR_MESSAGE);
        assertThat(NON_NULL_STRING).isEqualTo(result);
    }

    @Test
    public void checkNotNull_shouldThrow_withProvidedErrorMessage() {
        try {
            Preconditions.checkNotNull(null, ERROR_MESSAGE);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
            assertThat(npe).hasMessage(ERROR_MESSAGE);
        }
    }
}
