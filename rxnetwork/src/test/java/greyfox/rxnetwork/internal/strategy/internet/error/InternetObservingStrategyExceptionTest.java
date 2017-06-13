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
package greyfox.rxnetwork.internal.strategy.internet.error;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class InternetObservingStrategyExceptionTest {

  private static final String VALID_MESSAGE = "valid message";
  private final Throwable VALID_CAUSE = new Throwable();
  private InternetObservingStrategyException sut;

  @Test
  public void shouldHaveNoCause_andNoMessage() {
    sut = new InternetObservingStrategyException();

    assertThat(sut).hasNoCause().hasMessage(null);
  }

  @Test
  public void shouldHaveValidMessage() {
    sut = new InternetObservingStrategyException(VALID_MESSAGE);

    assertThat(sut).hasNoCause().hasMessage(VALID_MESSAGE);
  }

  @Test
  public void shouldHaveValidMessage_andValidCause() {
    sut = new InternetObservingStrategyException(VALID_MESSAGE, VALID_CAUSE);

    assertThat(sut).hasCause(VALID_CAUSE).hasMessage(VALID_MESSAGE);
  }

  @Test
  public void shouldHaveValidCause() {
    sut = new InternetObservingStrategyException(VALID_CAUSE);

    assertThat(sut).hasCause(VALID_CAUSE);
  }
}
