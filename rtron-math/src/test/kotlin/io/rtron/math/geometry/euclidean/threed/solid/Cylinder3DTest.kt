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

package io.rtron.math.geometry.euclidean.threed.solid

import com.github.kittinunf.result.Result
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import io.rtron.math.transform.AffineSequence3D


internal class Cylinder3DTest {

    @Nested
    inner class PolygonsGeneration {
        @Test
        fun `polygons start at level zero`() {
            val cylinder = Cylinder3D(0.5, 1.0, AffineSequence3D.EMPTY)

            val actualPolygonsResult = cylinder.calculatePolygonsGlobalCS()

            assertThat(actualPolygonsResult).isInstanceOf(Result.Success::class.java)
            require(actualPolygonsResult is Result.Success)
            assertTrue(actualPolygonsResult.value.any { polygon -> polygon.vertices.any { it.z == 0.0 } })
        }
    }
}
