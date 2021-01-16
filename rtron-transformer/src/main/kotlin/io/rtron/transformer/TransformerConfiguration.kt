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

package io.rtron.transformer

import io.rtron.io.files.FileIdentifier
import io.rtron.io.logging.LogManager

/**
 * Configuration class for a transformer holding general information, such as [projectId] and [sourceFileIdentifier].
 * The transformer specific parameter are provided in [parameters].
 *
 * @param projectId identifier of the current transformation project
 * @param sourceFileIdentifier identifier of model's source file
 * @param concurrentProcessing if true, apply concurrent processing
 * @param parameters transformer specific parameters
 */
class TransformerConfiguration<T : AbstractTransformerParameters>(
    val projectId: String,
    val sourceFileIdentifier: FileIdentifier,
    val concurrentProcessing: Boolean,
    val parameters: T
) {
    // Methods
    fun getReportLogger() = LogManager.getReportLogger(projectId)
}
