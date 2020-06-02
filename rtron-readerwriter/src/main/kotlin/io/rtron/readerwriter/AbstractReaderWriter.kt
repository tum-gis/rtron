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

package io.rtron.readerwriter

import com.github.kittinunf.result.Result
import io.rtron.io.files.Path
import io.rtron.model.AbstractModel

abstract class AbstractReaderWriter(
        open val configuration: AbstractReaderWriterConfiguration
) {

    // Properties and Initializers

    // Methods
    abstract fun isSupported(fileExtension: String): Boolean
    abstract fun isSupported(model: AbstractModel): Boolean

    abstract fun read(filePath: Path): AbstractModel

    abstract fun write(model: AbstractModel, directoryPath: Path): Result<List<Path>, Exception>

}
