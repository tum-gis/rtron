#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.0")
*/

/**
 * The company atlatec provides sample OpenDRIVE datasets, which can be downloaded here:
 * https://www.atlatec.de/getsampledata.html
 *
 * This is the script with configuration to transform their San Francisco OpenDRIVE dataset to CityGML2.
 */

import io.rtron.main.project.processAllFiles

val homeDirectory: String = System.getProperty("user.home")

processAllFiles(
    inInputDirectory = "$homeDirectory/Desktop/input-datasets",
    withExtension = "xodr",
    toOutputDirectory = "$homeDirectory/Desktop/output-datasets"
)
{
    val opendriveModel = readOpendriveModel(inputFilePath)
    val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
        crsEpsg = 32610
        offsetX = 500000.0 + 52300.0
        offsetY = 4182400.0
    }
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        discretizationStepSize = 0.5
        flattenGenericAttributeSets = true
    }
    writeCitygmlModel(citygmlModel)
}
