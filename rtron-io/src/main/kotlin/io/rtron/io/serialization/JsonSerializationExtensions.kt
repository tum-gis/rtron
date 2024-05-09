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

package io.rtron.io.serialization

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension

inline fun <reified T> T.serializeToJsonFile(filePath: Path) {
    require(filePath.extension == "json") { "Extension of file path must be json." }

    if (!filePath.parent.exists()) {
        filePath.parent.createDirectories()
    }

    val jsonFormatter =
        Json {
            prettyPrint = true
            encodeDefaults = true
        }

    val jsonFileContent = jsonFormatter.encodeToString(this)
    filePath.toFile().writeText(jsonFileContent)
}
