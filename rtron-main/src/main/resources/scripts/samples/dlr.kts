#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.1")
*/

/**
 * The German Aerospace Center (DLR) provides an OpenDRIVE dataset of the inner-city ring road of Brunswick,
 * which can be downloaded here: https://zenodo.org/record/4043193
 * The data can be complemented with LoD2 building models, which are provided as open data by Lower Saxony here:
 * https://opengeodata.lgln.niedersachsen.de/#lod2
 *
 * This is the script with configuration to transform their OpenDRIVE dataset to CityGML2.
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
        crsEpsg = 32632
        offsetX = 604763.0
        offsetY = 5792795.0

        // This is a manually estimated height offset, so that the OpenDRIVE dataset can be combined with the
        // LoD2 building models. Thus, take the value with caution.
        offsetZ = -43.28
    }
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        discretizationStepSize = 0.5
        flattenGenericAttributeSets = true
    }
    writeCitygmlModel(citygmlModel)
}
