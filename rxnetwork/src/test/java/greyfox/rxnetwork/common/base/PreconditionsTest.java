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
package greyfox.rxnetwork.common.base;

import org.junit.Test;

import static greyfox.rxnetwork.common.base.Preconditions.checkNotNull;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;

public class PreconditionsTest {

  private static final String NON_NULL_ARG = "non null arg";
  private static final String ERROR_MESSAGE_TEMPLATE = "error message with %s";
  private static final String ERROR_MESSAGE_ARG = "error arg";
  private static final String ERROR_MESSAGE_RESULT = "error message with error arg";

  @Test(expected = AssertionError.class)
  public void shouldThrow_whenTryingToInstantiateViaConstructor() {
    new Preconditions();
  }

  @Test
  public void shouldReturnCheckingReference_whenNotNull() {
    String result = checkNotNull(NON_NULL_ARG);
    assertThat(NON_NULL_ARG).isEqualTo(result);
  }

  @Test
  public void shouldReturnCheckingReference_whenNotNull_andWithErrorMessage() {
    String result = checkNotNull(NON_NULL_ARG, ERROR_MESSAGE_ARG);
    assertThat(NON_NULL_ARG).isEqualTo(result);
  }

  @Test
  public void shouldReturnCheckingReference_whenNotNull_andWithErrorMessageTemplate() {
    String result = Preconditions
        .checkNotNullWithMessage(NON_NULL_ARG, ERROR_MESSAGE_TEMPLATE, ERROR_MESSAGE_ARG);
    assertThat(NON_NULL_ARG).isEqualTo(result);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrow_whenNull() {
    checkNotNull(null);
  }

  @Test
  public void shouldThrow_withCorrectErrorMessage() {
    try {
      checkNotNull(null, ERROR_MESSAGE_ARG);
      fail("NullPointerException expected");
    } catch (NullPointerException npe) {
      assertThat(npe).hasMessageContaining(ERROR_MESSAGE_ARG);
    }
  }

  @Test
  public void shouldThrow_withCorrectErrorMessageFromTemplate() {
    try {
      Preconditions.checkNotNullWithMessage(null, ERROR_MESSAGE_TEMPLATE, ERROR_MESSAGE_ARG);
      fail("NullPointerException expected");
    } catch (NullPointerException npe) {
      assertThat(npe).hasMessage(ERROR_MESSAGE_RESULT);
    }
  }
}
