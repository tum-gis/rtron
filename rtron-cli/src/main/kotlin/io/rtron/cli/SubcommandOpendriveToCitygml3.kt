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

package io.rtron.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.triple
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.opendrive.OpendriveReader

class SubcommandOpendriveToCitygml3 : CliktCommand(name = "opendrive-to-citygml3", help = "Transform OpenDRIVE to CityGML 3.0.", printHelpOnEmptyArgs = true) {

    // Properties and Initializers
    init {
        context { helpFormatter = ColorFormatter() }
    }

    private val inputPath by argument(
        help = "Path to the directory containing OpenDRIVE datasets"
    ).path(mustExist = true)
    private val outputPath by argument(
        help = "Path to the output directory into which the transformed CityGML models are written"
    ).path()

    private val tolerance by option(help = "allowed tolerance when comparing double values").double()
    private val crsEpsg by option(help = "EPSG code of the coordinate reference system used in the OpenDRIVE datasets").int()
    private val offset by option(help = "offset values by which the model is translated along x, y, and z axis").double().triple()

    private val discretizationStepSize by option(help = "distance between each discretization step for curves and surfaces").double()
    private val sweepDiscretizationStepSize by option(help = "distance between each discretization step for solid geometries of ParametricSweep3D").double()
    private val circleSlices by option(help = "number of discretization points for a circle or cylinder").int()
    private val transformAdditionalRoadLines by option(help = "if true, additional road lines, such as the reference line, lane boundaries, etc. are also transformed").flag()

    // Methods

    override fun run() {

        processAllFiles(inInputDirectory = inputPath.toString(), withExtension = OpendriveReader.supportedFileExtensions.head, toOutputDirectory = outputPath.toString()) {
            val opendriveModel = readOpendriveModel(inputFilePath)
                .fold({ logger.warn(it.message); return@processAllFiles }, { it })

            val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
                this@SubcommandOpendriveToCitygml3.tolerance?.let { numberTolerance = it }
                this@SubcommandOpendriveToCitygml3.crsEpsg?.let { crsEpsg = it }
                offset?.let {
                    offsetX = it.first
                    offsetY = it.second
                    offsetZ = it.third
                }
            }.fold({ logger.warn(it.message); return@processAllFiles }, { it })

            val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
                flattenGenericAttributeSets = true
                this@SubcommandOpendriveToCitygml3.discretizationStepSize?.let { discretizationStepSize = it }
                this@SubcommandOpendriveToCitygml3.sweepDiscretizationStepSize?.let { sweepDiscretizationStepSize = it }
                this@SubcommandOpendriveToCitygml3.circleSlices?.let { circleSlices = it }
                transformAdditionalRoadLines = this@SubcommandOpendriveToCitygml3.transformAdditionalRoadLines
                mappingBackwardsCompatibility = false
            }

            writeCitygmlModel(citygmlModel) {
                versions = setOf(CitygmlVersion.V3_0)
            }
        }
    }
}
