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

import org.apache.commons.math3.util.FastMath as CMFastMath

// constants
val DBL_EPSILON = Math.ulp(1.0)
val DBL_EPSILON_1 = DBL_EPSILON
val DBL_EPSILON_2 = Math.ulp(1E2)
val DBL_EPSILON_3 = Math.ulp(1E3)
val DBL_EPSILON_4 = Math.ulp(1E4)
val DBL_EPSILON_5 = Math.ulp(1E5)
val DBL_EPSILON_6 = Math.ulp(1E6)
val DBL_EPSILON_7 = Math.ulp(1E7)
val DBL_EPSILON_8 = Math.ulp(1E8)
val DBL_EPSILON_9 = Math.ulp(1E9)
val DBL_EPSILON_10 = Math.ulp(1E10)

val FLT_EPSILON = Math.ulp(1.0f)

val DEFAULT_TOLERANCE = DBL_EPSILON_7

/** Value of PI as double. (180 degrees) */
const val PI = CMFastMath.PI
/** Value of 2PI as double. (360 degrees) */
const val TWO_PI = 2.0 * PI
/** Value of PI/2 as double. (90 degrees) */
const val HALF_PI = 0.5 * PI
/** Value of PI/4 as double. (45 degrees) */
const val QUARTER_PI = 0.25 * PI

/** Value of 1/PI as double. */
const val INV_PI = 1.0 / PI
/** Value of 1/(2PI) as double. */
const val INV_TWO_PI = 1.0 / TWO_PI

/** Value to multiply a degree value by, to convert to radians. */
const val DEG_TO_RAD = PI / 180.0
/** Value to multiply a radian value by, to convert to degrees. */
const val RAD_TO_DEG = 180.0 / PI
