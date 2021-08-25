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
 * This is the script with configuration to transform their Spain (El Vendrell) OpenDRIVE dataset to CityGML2.
 */

import io.rtron.main.project.processAllFiles

processAllFiles(
    inInputDirectory = "/project/input", // adjust path to directory of input datasets
    withExtension = "xodr",
    toOutputDirectory = "/project/output" // adjust path to output directory
)
{
    val opendriveModel = readOpendriveModel(inputFilePath)
    val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
        crsEpsg = 32631
        // offsets are determined by a PROJ4 transformation from the PROJ string of the OpenDRIVE to
        // the PROJ string of EPSG:32631
        offsetX = 376992.8779778732
        offsetY = 4564830.71808729
    }
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        discretizationStepSize = 0.5
        flattenGenericAttributeSets = true
    }
    writeCitygmlModel(citygmlModel)
}
