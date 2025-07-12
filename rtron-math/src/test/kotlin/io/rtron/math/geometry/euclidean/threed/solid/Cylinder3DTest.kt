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

package io.rtron.math.geometry.euclidean.threed.solid

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.rtron.math.std.DBL_EPSILON_1
import io.rtron.math.transform.AffineSequence3D

class Cylinder3DTest :
    FunSpec({
        context("PolygonsGeneration") {

            test("polygons start at level zero") {
                val cylinder = Cylinder3D(0.5, 1.0, DBL_EPSILON_1, AffineSequence3D.EMPTY)

                val actualPolygons = cylinder.calculatePolygonsGlobalCS()

                actualPolygons.any { polygon -> polygon.vertices.any { it.z == 0.0 } }.shouldBeTrue()
            }
        }
    })
