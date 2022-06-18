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

import arrow.core.toOption
import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.triple
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import io.rtron.io.files.File
import io.rtron.io.files.Path
import io.rtron.io.files.toPath
import io.rtron.main.processor.OpendriveToCitygmlConfiguration
import io.rtron.main.processor.OpendriveToCitygmlProcessor
import io.rtron.transformer.converter.opendrive2roadspaces.configuration.Opendrive2RoadspacesConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration

class SubcommandOpendriveToCitygml : CliktCommand(name = "opendrive-to-citygml", help = "Transform OpenDRIVE datasets to CityGML.", printHelpOnEmptyArgs = true) {

    // Properties and Initializers
    init {
        context { helpFormatter = ColorFormatter() }
    }

    private val configurationPath by option(
        help = "Path to a YAML configuration of the process."
    ).path(mustExist = true)

    private val inputPath by argument(
        help = "Path to the directory containing OpenDRIVE datasets"
    ).path(mustExist = true)
    private val outputPath by argument(
        help = "Path to the output directory into which the transformed CityGML models are written"
    ).path()

    private val convertToCitygml2 by option(help = "convert to CityGML 2.0 (otherwise CityGML 3.0)").flag()

    private val tolerance by option(help = "allowed tolerance when comparing double values").double()
        .default(Opendrive2RoadspacesConfiguration.DEFAULT_NUMBER_TOLERANCE)
    private val crsEpsg by option(help = "EPSG code of the coordinate reference system used in the OpenDRIVE datasets").int()
        .default(Opendrive2RoadspacesConfiguration.DEFAULT_CRS_EPSG)
    private val offset by option(help = "offset values by which the model is translated along x, y, and z axis").double().triple()
        .default(Triple(Opendrive2RoadspacesConfiguration.DEFAULT_OFFSET_X, Opendrive2RoadspacesConfiguration.DEFAULT_OFFSET_Y, Opendrive2RoadspacesConfiguration.DEFAULT_OFFSET_Z))

    private val discretizationStepSize by option(help = "distance between each discretization step for curves and surfaces").double()
        .default(Roadspaces2CitygmlConfiguration.DEFAULT_DISCRETIZATION_STEP_SIZE)
    private val sweepDiscretizationStepSize by option(help = "distance between each discretization step for solid geometries of ParametricSweep3D").double()
        .default(Roadspaces2CitygmlConfiguration.DEFAULT_SWEEP_DISCRETIZATION_STEP_SIZE)
    private val circleSlices by option(help = "number of discretization points for a circle or cylinder").int()
        .default(Roadspaces2CitygmlConfiguration.DEFAULT_CIRCLE_SLICES)
    private val transformAdditionalRoadLines by option(help = "if true, additional road lines, such as the reference line, lane boundaries, etc. are also transformed").flag()

    // Methods

    override fun run() {

        val configuration = configurationPath.toOption().fold({
            OpendriveToCitygmlConfiguration(
                convertToCitygml2 = convertToCitygml2,

                tolerance = tolerance,
                crsEpsg = crsEpsg,
                offsetX = offset.first,
                offsetY = offset.second,
                offsetZ = offset.third,

                discretizationStepSize = discretizationStepSize,
                sweepDiscretizationStepSize = sweepDiscretizationStepSize,
                circleSlices = circleSlices,
                transformAdditionalRoadLines = transformAdditionalRoadLines,
            )
        }, {
            val configurationFilePath = Path.of(it)
            val configurationText = File(configurationFilePath).readText()

            Yaml.default.decodeFromString(OpendriveToCitygmlConfiguration.serializer(), configurationText)
        })

        val processor = OpendriveToCitygmlProcessor(configuration)
        processor.process(inputPath.toPath(), outputPath.toPath())
    }
}
