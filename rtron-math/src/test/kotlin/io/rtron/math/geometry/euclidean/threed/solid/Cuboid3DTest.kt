/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.geometry.euclidean.threed.solid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D

class Cuboid3DTest : FunSpec({
    context("PolygonsGeneration") {

        test("correct number of polygons") {
            val cuboid = Cuboid3D.UNIT

            val actualPolygons = cuboid.calculatePolygonsGlobalCS()

            actualPolygons shouldHaveSize 6
        }

        test("generated polygons list contain base polygon") {
            val length = 6.0
            val width = 4.0
            val cuboid = Cuboid3D(length = length, width = width, height = 1.0, tolerance = 0.0)
            val vertexA = Vector3D(length / 2.0, width / 2.0, 0.0)
            val vertexB = Vector3D(length / 2.0, -width / 2.0, 0.0)
            val vertexC = Vector3D(-length / 2.0, -width / 2.0, 0.0)
            val vertexD = Vector3D(-length / 2.0, width / 2.0, 0.0)
            val expectedBasePolygon = Polygon3D.of(vertexA, vertexB, vertexC, vertexD, tolerance = 0.0)

            val actualPolygons = cuboid.calculatePolygonsGlobalCS()

            actualPolygons.shouldContain(expectedBasePolygon)
        }

        test("generated polygons list contain elevated polygon") {
            val length = 5.0
            val width = 3.0
            val height = 1.0
            val cuboid = Cuboid3D(length = length, width = width, height = height, tolerance = 0.0)
            val vertexA = Vector3D(length / 2.0, width / 2.0, height)
            val vertexB = Vector3D(-length / 2.0, width / 2.0, height)
            val vertexC = Vector3D(-length / 2.0, -width / 2.0, height)
            val vertexD = Vector3D(length / 2.0, -width / 2.0, height)
            val expectedBasePolygon = Polygon3D.of(vertexA, vertexB, vertexC, vertexD, tolerance = 0.0)

            val actualPolygons = cuboid.calculatePolygonsGlobalCS()

            actualPolygons.shouldContain(expectedBasePolygon)
        }

        test("generated polygons list contain front polygon") {
            val length = 5.0
            val width = 3.0
            val height = 1.0
            val cuboid = Cuboid3D(length = length, width = width, height = height, tolerance = 0.0)
            val vertexA = Vector3D(length / 2.0, width / 2.0, 0.0)
            val vertexB = Vector3D(length / 2.0, width / 2.0, height)
            val vertexC = Vector3D(length / 2.0, -width / 2.0, height)
            val vertexD = Vector3D(length / 2.0, -width / 2.0, 0.0)
            val expectedBasePolygon = Polygon3D.of(vertexA, vertexB, vertexC, vertexD, tolerance = 0.0)

            val actualPolygons = cuboid.calculatePolygonsGlobalCS()

            actualPolygons.shouldContain(expectedBasePolygon)
        }
    }
})
