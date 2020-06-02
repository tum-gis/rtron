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

package io.rtron.std

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


internal class SetsKtTest {

    @Nested
    inner class TestCombinations {

        @Test
        fun `test basic combination generation`() {
            val startSet = setOf("a", "b", "c")
            val expectedCombinations = setOf(
                    setOf("a", "b"), setOf("a", "c"), setOf("b", "c"))

            val actualCombinations = startSet.combinations(2)

            assertThat(actualCombinations).isEqualTo(expectedCombinations)
        }
    }

}
