/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.io.files

import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.fileSize
import kotlin.streams.asSequence

/**
 * Returns a human-readable file size like 1MB or 1GB.
 *
 * @return human-readable size of a file
 */
fun Path.getFileSizeToDisplay(): String = FileUtils.byteCountToDisplaySize(this.fileSize())

/**
 * Returns a sequence of subpaths with [maxDepth] by walking the file tree.
 *
 * @param maxDepth maximal depth to be traversed
 * @return sequence of [Path]
 */
fun Path.walk(maxDepth: Int = Int.MAX_VALUE): Sequence<Path> =
    Files.walk(this, maxDepth).asSequence().map { it }
