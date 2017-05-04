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

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import static java.lang.String.format;

import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;

/**
 * Static convenience methods that help a method or constructor check whether it was invoked
 * correctly (whether its <i>preconditions</i> have been met). In the case of {@code
 * checkNotNullWithMessage}, an object reference which is expected to be non-null). When {@code
 * false} (or
 * {@code null}) is passed instead, the {@code Preconditions} method throws an unchecked exception,
 * which helps the calling method communicate to <i>its</i> caller that <i>that</i> caller has made
 * a mistake.
 *
 * @author Radek Kozak
 */
@RestrictTo(LIBRARY_GROUP)
public final class Preconditions {

    @VisibleForTesting(otherwise = PRIVATE)
    Preconditions() {
        throw new AssertionError("No instances");
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     *
     * @return the non-null reference that was validated
     *
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference) {
        return checkNotNull(reference, "Provided reference must not be null");
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param name      an object reference name
     *
     * @return the non-null reference that was validated
     *
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference, String name) {
        if (reference == null) throw new NullPointerException(name + " must not be null");
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param message   exception message should the check fail
     *
     * @return the non-null reference that was validated
     *
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNullWithMessage(T reference, String message) {

        if (reference == null) throw new NullPointerException(message);
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference     an object reference
     * @param messageFormat exception message as described in
     *                      <a href="../util/Formatter.html#syntax">format string</a>
     * @param args          arguments referenced by the {@code messageFormat} specifiers
     *
     * @return the non-null reference that was validated
     *
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNullWithMessage(T reference, String messageFormat, Object... args) {

        if (reference == null) throw new NullPointerException(format(messageFormat, args));
        return reference;
    }
}
