#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.0")
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
        discretizationStepSize = 0.5
        flattenGenericAttributeSets = true
    }
    writeCitygmlModel(citygmlModel)
}
