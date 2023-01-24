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

object Project {
    const val name = "rtron"
    const val group = "io.rtron"
    const val version = "1.3.0-alpha.0"
}

object ProjectComponents {
    // user interface layer
    const val main = ":rtron-main"
    const val documentation = ":rtron-documentation"

    // model transformation layer
    const val model = ":rtron-model"
    const val readerWriter = ":rtron-readerwriter"
    const val transformer = ":rtron-transformer"

    // utility layer
    const val math = ":rtron-math"
    const val standard = ":rtron-std"
    const val inputOutput = ":rtron-io"
}
