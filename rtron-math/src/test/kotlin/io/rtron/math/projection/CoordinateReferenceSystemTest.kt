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

package io.rtron.math.projection

import arrow.core.Either
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CoordinateReferenceSystemTest :
    FunSpec({

        context("TestProperties") {

            test("build crs 4326 from epsg name") {
                val crsName = "EPSG:4326"

                val actualCrsResult = CoordinateReferenceSystem.of(crsName)

                require(actualCrsResult is Either.Right)
                actualCrsResult.value.name shouldBe crsName
            }

            test("build crs 32632 from epsg name") {
                val crsName = "EPSG:32632"

                val actualCrsResult = CoordinateReferenceSystem.of(crsName)

                require(actualCrsResult is Either.Right)
                actualCrsResult.value.name shouldBe crsName
            }

            test("extract epsg code 4326") {
                val crsName = "EPSG:4326"

                val actualCrsResult = CoordinateReferenceSystem.of(crsName)

                require(actualCrsResult is Either.Right)
                actualCrsResult.value.epsgCode shouldBe 4326
            }

            test("extract epsg code 32632") {
                val crsName = "EPSG:32632"

                val actualCrsResult = CoordinateReferenceSystem.of(crsName)

                require(actualCrsResult is Either.Right)
                actualCrsResult.value.epsgCode shouldBe 32632
            }

            test("build crs 32632 from parameters") {
                val parameters = "+proj=utm +zone=32 +datum=WGS84 +units=m +no_defs"

                val actualCrsResult = CoordinateReferenceSystem.ofParameters(parameters)

                require(actualCrsResult is Either.Right)
                actualCrsResult.value.epsgCode shouldBe 32632
            }

            test("build crs 4326 from parameters") {
                val parameters = "+proj=longlat +datum=WGS84 +no_defs"

                val actualCrsResult = CoordinateReferenceSystem.ofParameters(parameters)

                require(actualCrsResult is Either.Right)
                actualCrsResult.value.epsgCode shouldBe 4326
            }
        }
    })
