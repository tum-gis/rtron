#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.0")
*/

import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlVersion

val homeDirectory: String = System.getProperty("user.home")

/**
 * This function iterates over all files contained in the input directory that have the extension "xodr".
 */
processAllFiles(
    inInputDirectory = "$homeDirectory/Desktop/input-datasets",
    withExtension = "xodr",
    toOutputDirectory = "$homeDirectory/Desktop/output-datasets"
)
{
    // Within this block the transformations can be defined by the user. For example:

    // 1. Read the OpenDRIVE dataset into memory:
    val opendriveModel = readOpendriveModel(inputFilePath)

    // 2. Transform the OpenDRIVE model to an intermediary representation (the RoadSpaces model):
    val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
        // Within this blocks, the transformation is parametrized:
        crsEpsg = 32632 // EPSG code of the coordinate reference system (obligatory for working with GIS applications)
    }

    // 3. Transform the RoadSpaces model to a CityGML model:
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        flattenGenericAttributeSets = true // true, if nested attribute lists shall be flattened out
        discretizationStepSize = 0.5 // distance between each discretization step for curves and surfaces
    }

    // 4. Write the CityGML model to the output directory:
    writeCitygmlModel(citygmlModel) {
        versions = setOf(CitygmlVersion.V2_0) // set the CityGML versions for writing
    }
}
