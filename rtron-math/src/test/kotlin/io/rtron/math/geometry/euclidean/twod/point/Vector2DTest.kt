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

package io.rtron.math.geometry.euclidean.twod.point

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.rtron.math.std.HALF_PI
import io.rtron.math.std.PI
import kotlin.math.sqrt

class Vector2DTest : FunSpec({

    fun scalarMultiply() {
        val actualValue = Vector2D(1.0, 1.0).scalarMultiply(4.0)

        actualValue shouldBe Vector2D(4.0, 4.0)
    }

    context("TestDistanceCalculation") {

        test("even value distance between two point") {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(3.0, 0.0)

            val actualDistance = pointA.distance(pointB)

            actualDistance shouldBe 3.0
        }

        test("uneven value distance between two point") {
            val pointA = Vector2D(0.0, 0.0)
            val pointB = Vector2D(1.0, 1.0)

            val actualDistance = pointA.distance(pointB)

            actualDistance shouldBe sqrt(2.0)
        }
    }

    context("TestAngleCalculation") {

        test("half pi angle between to axes") {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D.Y_AXIS

            val actualAngle = pointA.angle(pointB).toAngleRadians()

            actualAngle shouldBe HALF_PI
        }

        test("uneven radians angle") {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D(1.0, -1.0)

            val actualAngle = pointA.angle(pointB).toAngleRadians()

            actualAngle shouldBe 1.75 * PI
        }

        test("-half pi degree angle axes") {
            val pointA = Vector2D.X_AXIS
            val pointB = Vector2D(0.0, -1.0)

            val actualAngle = pointA.angle(pointB).toAngleRadians()

            actualAngle shouldBe 1.5 * PI
        }
    }
})
