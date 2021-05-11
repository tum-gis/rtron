#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.0")
*/

import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlVersion

/**
 * This script converts OpenDRIVE datasets to CityGML3.
 * Make sure to disable the backwards compatibility option and
 */

val homeDirectory: String = System.getProperty("user.home")

processAllFiles(
    inInputDirectory = "$homeDirectory/Desktop/input-datasets",
    withExtension = "xodr",
    toOutputDirectory = "$homeDirectory/Desktop/output-datasets"
)
{
    val opendriveModel = readOpendriveModel(inputFilePath)
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
