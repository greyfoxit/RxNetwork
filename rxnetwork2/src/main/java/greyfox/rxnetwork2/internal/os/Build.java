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
package greyfox.rxnetwork2.internal.os;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.support.annotation.VisibleForTesting.PRIVATE;

import android.support.annotation.VisibleForTesting;

/**
 * This class contains platform version checking methods for testing
 * compatibility with platform features.
 *
 * @author Radek Kozak
 */
@SuppressWarnings("WeakerAccess")
public final class Build {

    @VisibleForTesting(otherwise = PRIVATE)
    Build() {
        throw new AssertionError("No instances.");
    }

    public static boolean isAtLeastLollipop() {
        return SDK_INT >= LOLLIPOP;
    }

    public static boolean isAtLeastMarshmallow() {
        return SDK_INT >= M;
    }

    public static boolean isLessThanLollipop() {
        return SDK_INT < LOLLIPOP;
    }

    public static boolean isLessThanMarshmallow() {
        return SDK_INT < M;
    }
}

