/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.std

import org.apache.commons.math3.util.MathUtils
import kotlin.math.abs
import com.google.common.math.DoubleMath as GDoubleMath

/**
 * Returns if [a] equals [b] with a defined [tolerance].
 *
 * @param a first value
 * @param b second value
 * @param tolerance allowed tolerance
 * @return true, if abs(a - b) <= tolerance
 */
fun fuzzyEquals(a: Double, b: Double, tolerance: Double): Boolean =
        if (tolerance == 0.0) a == b
        else GDoubleMath.fuzzyEquals(a, b, tolerance)

/**
 * Returns the comparison of [a] and [b] with a defined [tolerance].
 *
 * @param a first value
 * @param b second value
 * @param tolerance allowed tolerance
 * @return 0 for a fuzzyEquals b; -1 for a < b; 1 for a > b
 */
fun fuzzyCompare(a: Double, b: Double, tolerance: Double) =
        GDoubleMath.fuzzyCompare(a, b, tolerance)

/**
 * Returns true, if [a] <= [b] with a [tolerance].
 *
 * @param a first value
 * @param b second value
 * @param tolerance allowed tolerance
 * @return true, if [a] <= [b] with a [tolerance]; false otherwise
 */
fun fuzzyLessThanOrEquals(a: Double, b: Double, tolerance: Double) =
        a <= (b + abs(tolerance)) || a == b || a.isNaN() && b.isNaN()

/**
 * Returns true, if [a] >= [b] with a [tolerance].
 *
 * @param a first value
 * @param b second value
 * @param tolerance allowed tolerance
 * @return true, if [a] >= [b] with a [tolerance]; false otherwise
 */
fun fuzzyMoreThanOrEquals(a: Double, b: Double, tolerance: Double) =
        a >= (b + abs(tolerance)) || a == b || a.isNaN() && b.isNaN()

/**
 * Normalizes an [angle] around the [center].
 *
 * @param angle angle for normalization in radians
 * @param center center of the desired normalization interval
 * @return normalized angle
 */
fun normalizeAngle(angle: Double, center: Double = PI) = MathUtils.normalizeAngle(angle, center)
