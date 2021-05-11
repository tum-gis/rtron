#!/usr/bin/env kscript
/*
@file:KotlinOpts("-J-Xmx50g")
@file:CompilerOpts("-jvm-target 1.8")
@file:DependsOn("io.rtron:rtron-main:1.2.0")
*/

/**
 * This script lists all the different configuration options to transform OpenDRIVE to CityGML.
 */

import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlVersion

val homeDirectory: String = System.getProperty("user.home")

processAllFiles(
    inInputDirectory = "$homeDirectory/Desktop/input-datasets",
    withExtension = "xodr",
    toOutputDirectory = "$homeDirectory/Desktop/output-datasets"
)
{

    val opendriveModel = readOpendriveModel(inputFilePath)
    val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
        // allowed tolerance when comparing double values
        tolerance = 1E-7

        // prefix of attribute names
        attributesPrefix = "opendrive_"

        // EPSG code of the coordinate reference system (obligatory for working with GIS applications)
        crsEpsg = 32632

        // offset by which the model is translated along the respective axis
        offsetX = 0.0
        offsetY = 0.0
        offsetZ = 0.0
    }
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        // prefix for generated gml ids
        gmlIdPrefix = "UUID_"

        // prefix for identifier attribute names
        identifierAttributesPrefix = "identifier_"

        // true, if nested attribute lists shall be flattened out
        flattenGenericAttributeSets = true

        // distance between each discretization step for curves and surfaces
        discretizationStepSize = 0.5

        // distance between each discretization step for solid geometries of ParametricSweep3D
        sweepDiscretizationStepSize = 0.3

        // number of discretization points for a circle or cylinder
        circleSlices = 12

        // true, if random ids shall be generated for the gml geometries
        generateRandomGeometryIds = true

        // if true, additional road lines (reference line, lane boundaries) are exported
        transformAdditionalRoadLines = false

        // if true, only classes are populated that are also available in CityGML2
        mappingBackwardsCompatibility = true
    }

    writeCitygmlModel(citygmlModel) {
        // set the CityGML versions for writing
        versions = setOf(CitygmlVersion.V2_0, CitygmlVersion.V3_0)
    }
}
