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

package io.rtron.math.geometry.euclidean.twod.surface

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.rtron.math.geometry.euclidean.twod.point.Vector2D

class Polygon2DTest : FunSpec({
    context("TestContainsCalculation") {

        test("basic triangle contains point") {
            val vertices: NonEmptyList<Vector2D> = listOf(Vector2D.ZERO, Vector2D.X_AXIS, Vector2D.Y_AXIS).toNonEmptyListOrNull()!!
            val polygon = Polygon2D(vertices, 0.0)

            val actualReturn = polygon.contains(Vector2D(0.25, 0.25))

            actualReturn.shouldBeTrue()
        }

        test("basic triangle does not contain point") {
            val vertices: NonEmptyList<Vector2D> = listOf(
                Vector2D.ZERO,
                Vector2D.X_AXIS,
                Vector2D.Y_AXIS
            ).toNonEmptyListOrNull()!!
            val polygon = Polygon2D(vertices, 0.0)

            val actualReturn = polygon.contains(Vector2D(1.25, 1.25))

            actualReturn.shouldBeFalse()
        }

        test("concave polygon does contain point") {
            val vertices: NonEmptyList<Vector2D> = listOf(
                Vector2D.ZERO,
                Vector2D(1.0, 1.0),
                Vector2D(2.0, 0.0),
                Vector2D(2.0, 3.0),
                Vector2D(0.0, 3.0)
            ).toNonEmptyListOrNull()!!
            val polygon = Polygon2D(vertices, 0.0)

            val actualReturn = polygon.contains(Vector2D(1.0, 1.1))

            actualReturn.shouldBeTrue()
        }
    }
})
