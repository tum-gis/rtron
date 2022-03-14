#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.2")
*/

import arrow.core.Some
import io.rtron.main.project.processAllFiles
import io.rtron.model.opendrive.OpendriveModel

/**
 * This script iterates over OpenDRIVE datasets, validates and corrects them.
 */
processAllFiles(
    inInputDirectory = "/project/input", // adjust path to directory of input datasets
    withExtension = "xodr",
    toOutputDirectory = "/project/output" // adjust path to output directory
)
{
    val opendriveModel: OpendriveModel = readOpendriveModel(inputFilePath) {
        outputSchemaValidationReportDirectoryPath = Some(outputDirectoryPath)
    }.fold( { logger.warn(it.message); return@processAllFiles }, { it }) // TODO

    logger.info(opendriveModel.header.toString())
}
