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

package io.rtron.readerwriter.opendrive

import arrow.core.None
import arrow.core.Option
import io.rtron.io.files.CompressedFileExtension

data class OpendriveWriterParameters(
    val fileCompression: Option<CompressedFileExtension> = DEFAULT_FILE_COMPRESSION
) {

    companion object {
        val DEFAULT_FILE_COMPRESSION = None
    }
}
