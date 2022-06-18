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

import io.rtron.io.files.Path
import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlWriter
import io.rtron.readerwriter.opendrive.OpendriveReader
import io.rtron.readerwriter.opendrive.OpendriveWriter
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesTransformer
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlTransformer
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluator
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluator

class ValidateOpendriveProcessor(
    private val configuration: ValidateOpendriveConfiguration
) {

    fun process(inputPath: Path, outputPath: Path) {

        processAllFiles(
            inputDirectoryPath = inputPath,
            withExtension = OpendriveReader.supportedFileExtensions.head,
            outputDirectoryPath = outputPath
        ) {

            // read OpenDRIVE model
            val opendriveReaderConfiguration = configuration.deriveOpendriveReaderConfiguration(projectConfiguration)
            val opendriveReader = OpendriveReader(opendriveReaderConfiguration)
            val opendriveModel = opendriveReader.read(projectConfiguration.inputFilePath)
                .fold({ logger.warn(it.message); return@processAllFiles }, { it })

            // evaluate OpenDRIVE model
            val opendriveEvaluatorConfiguration = configuration.deriveOpendriveEvaluatorConfiguration(projectConfiguration)
            val opendriveEvaluator = OpendriveEvaluator(opendriveEvaluatorConfiguration)
            val healedOpendriveModel = opendriveEvaluator.evaluate(opendriveModel)
                .fold({ logger.warn(it.message); return@processAllFiles }, { it })

            // write healed OpenDRIVE model
            val opendriveWriterConfiguration = configuration.deriveOpendriveWriterConfiguration(projectConfiguration)
            val opendriveWriter = OpendriveWriter(opendriveWriterConfiguration)
            opendriveWriter.write(healedOpendriveModel, projectConfiguration.outputDirectoryPath)

            // transform OpenDRIVE model to Roadspaces model
            val opendrive2RoadspacesConfiguration = configuration.deriveOpendrive2RoadspacesConfiguration(projectConfiguration)
            val opendrive2RoadspacesTransformer = Opendrive2RoadspacesTransformer(opendrive2RoadspacesConfiguration)
            val roadspacesModel = opendrive2RoadspacesTransformer.transform(opendriveModel).fold({ logger.warn(it.message); return@processAllFiles }, { it })

            // evaluate Roadspaces model
            val roadspacesEvaluatorConfiguration = configuration.deriveRoadspacesEvaluatorConfiguration(projectConfiguration)
            val roadspacesEvaluator = RoadspacesEvaluator(roadspacesEvaluatorConfiguration)
            roadspacesEvaluator.evaluate(roadspacesModel)

            // transform Roadspaces model to CityGML model
            val roadspaces2CitygmlConfiguration = configuration.deriveRoadspaces2CitygmlConfiguration(projectConfiguration)
            val roadspaces2CitygmlTransformer = Roadspaces2CitygmlTransformer(roadspaces2CitygmlConfiguration)
            val citygmlModel = roadspaces2CitygmlTransformer.transform(roadspacesModel)

            // write CityGML model
            val citygmlWriterConfiguration = configuration.deriveCitygmlWriterConfiguration(projectConfiguration)
            val citygmlWriter = CitygmlWriter(citygmlWriterConfiguration)
            citygmlWriter.write(citygmlModel, projectConfiguration.outputDirectoryPath)
        }
    }
}
