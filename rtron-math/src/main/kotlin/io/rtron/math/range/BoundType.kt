/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rtron.math.range

import com.google.common.collect.BoundType as GBoundType

/**
 * Bound types of intervals.
 * See wikipedia article of [intervals](https://en.wikipedia.org/wiki/Interval_(mathematics)).
 */
enum class BoundType {
    /** open bound, such as the lower bound in (3,5] */
    OPEN,

    /** closed bound, such as the upper bound in (3,5] */
    CLOSED,

    /** no bound, such as the upper bound in [3, ∞) */
    NONE,
}

// Conversions

/** Conversion from Guava Bound Type class. */
fun GBoundType.toBoundType() =
    when (this) {
        GBoundType.OPEN -> BoundType.OPEN
        GBoundType.CLOSED -> BoundType.CLOSED
    }

/** Conversion to Guava Bound Type class. */
fun BoundType.toBoundTypeG(): GBoundType? =
    when (this) {
        BoundType.OPEN -> GBoundType.OPEN
        BoundType.CLOSED -> GBoundType.CLOSED
        BoundType.NONE -> null
    }
