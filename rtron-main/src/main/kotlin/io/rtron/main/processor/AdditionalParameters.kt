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

package io.rtron.main.processor

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import io.rtron.io.files.CompressedFileExtension

enum class CompressionFormat {
    NONE,
    GZ,
    ZIP,
    ZST
}

fun CompressionFormat.toOptionalCompressedFileExtension(): Option<CompressedFileExtension> = when (this) {
    CompressionFormat.NONE -> None
    CompressionFormat.GZ -> CompressedFileExtension.GZ.some()
    CompressionFormat.ZIP -> CompressedFileExtension.ZIP.some()
    CompressionFormat.ZST -> CompressedFileExtension.ZST.some()
}
