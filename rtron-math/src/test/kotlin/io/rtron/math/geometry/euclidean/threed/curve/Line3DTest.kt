/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.math.geometry.euclidean.threed.curve

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.rtron.math.geometry.euclidean.threed.point.Vector3D

class Line3DTest :
    FunSpec({
        context("Addition") {

            test("throws error if ") {
                val point = Vector3D(1.0, 1.0, 1.0)

                shouldThrow<IllegalArgumentException> {
                    Line3D(point, point, 0.0)
                }
            }
        }
    })
