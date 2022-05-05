#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.2")
*/

/**
 * The company 3D Mapping Solutions provides sample OpenDRIVE datasets, which can be downloaded here (after initial registration):
 * https://www.3d-mapping.de/en/customer-area/demo-data/
 *
 * This is the script with configuration to transform their OpenDRIVE dataset to CityGML2.
 *
 * Besides, this configuration is also used in the research project SAVe (https://save-in.digital).
 */

import io.rtron.main.project.processAllFiles

processAllFiles(
    inInputDirectory = "/project/input", // adjust path to directory of input datasets
    withExtension = "xodr",
    toOutputDirectory = "/project/output" // adjust path to output directory
)
{
    val opendriveModel = readOpendriveModel(inputFilePath)
        .fold( { logger.warn(it.message); return@processAllFiles }, { it })

    val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
        crsEpsg = 32632
    }.fold( { logger.warn(it.message); return@processAllFiles }, { it })
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        discretizationStepSize = 0.5
        flattenGenericAttributeSets = true
    }
    writeCitygmlModel(citygmlModel)
}
