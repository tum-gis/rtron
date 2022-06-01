#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.2")
*/

import io.rtron.main.project.processAllFiles
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.readerwriter.citygml.CitygmlVersion
import io.rtron.readerwriter.opendrive.OpendriveWriter
import io.rtron.readerwriter.opendrive.configuration.OpendriveWriterConfiguration
import io.rtron.transformer.evaluator.opendrive.OpendriveEvaluator
import io.rtron.transformer.evaluator.opendrive.configuration.OpendriveEvaluatorConfiguration
import io.rtron.transformer.evaluator.roadspaces.configuration.RoadspacesEvaluatorConfiguration
import io.rtron.transformer.evaluator.roadspaces.RoadspacesEvaluator

/**
 * This script iterates over OpenDRIVE datasets, validates and corrects them.
 */
processAllFiles(
    inInputDirectory = "/project/input", // adjust path to directory of input datasets
    withExtension = "xodr",
    toOutputDirectory = "/project/output" // adjust path to output directory
)
{
    val generalNumberTolerance = 1E-4
    val generalDistanceTolerance = 1E-2
    val generalAngleTolerance = 1E-2

    val opendriveModel: OpendriveModel = readOpendriveModel(inputFilePath) {
        outputSchemaValidationReportDirectoryPath = outputDirectoryPath
    }.fold( { logger.warn(it.message); return@processAllFiles }, { it })

    val opendriveEvaluatorConfiguration = OpendriveEvaluatorConfiguration(
        projectId, inputFileIdentifier, concurrentProcessing, outputDirectoryPath, generalNumberTolerance
    )
    val opendriveEvaluator = OpendriveEvaluator(opendriveEvaluatorConfiguration)
    val healedOpendriveModel = opendriveEvaluator.evaluate(opendriveModel)
        .fold( { logger.warn(it.message); return@processAllFiles }, { it })

    val opendriveWriterConfiguration = OpendriveWriterConfiguration(projectId)
    val opendriveWriter = OpendriveWriter(opendriveWriterConfiguration)
    opendriveWriter.write(healedOpendriveModel, outputDirectoryPath)


    val roadspacesModel = transformOpendrive2Roadspaces(healedOpendriveModel) {
        outputReportDirectoryPath = outputDirectoryPath

        numberTolerance = generalNumberTolerance
        distanceTolerance = generalDistanceTolerance
        angleTolerance = generalAngleTolerance

        crsEpsg = 32632
    }.fold( { logger.warn(it.message); return@processAllFiles }, { it })

    val roadspacesEvaluatorConfiguration = RoadspacesEvaluatorConfiguration(
        projectId, inputFileIdentifier, concurrentProcessing, outputDirectoryPath, generalNumberTolerance, generalDistanceTolerance
    )
    val roadspacesEvaluator = RoadspacesEvaluator(roadspacesEvaluatorConfiguration)
    roadspacesEvaluator.evaluate(roadspacesModel)

    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        outputReportDirectoryPath = outputDirectoryPath

        flattenGenericAttributeSets = true
        discretizationStepSize = 0.5
        transformAdditionalRoadLines = true
        generateLongitudinalFillerSurfaces = false
    }

    writeCitygmlModel(citygmlModel) {
        versions = setOf(CitygmlVersion.V2_0)
    }
}
