#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.2")
*/

import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlVersion

/**
 * This script converts OpenDRIVE datasets to CityGML3.
 * Make sure to disable the backwards compatibility option and to enable the writing of CityGML3.
 */
processAllFiles(
    inInputDirectory = "/project/input", // adjust path to directory of input datasets
    withExtension = "xodr",
    toOutputDirectory = "/project/output" // adjust path to output directory
)
{
    val opendriveModel = readOpendriveModel(inputFilePath).fold( { logger.warn(it.message); return@processAllFiles }, { it }) // TODO
    val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
        crsEpsg = 32632
    }
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        // if false, all classes according to CityGML3 are populated
        mappingBackwardsCompatibility = false
    }

    writeCitygmlModel(citygmlModel) {
        // write as CityGML dataset of version 3
        versions = setOf(CitygmlVersion.V3_0)
    }
}
