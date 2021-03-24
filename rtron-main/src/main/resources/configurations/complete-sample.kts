import io.rtron.main.project.configuration.configure
import io.rtron.readerwriter.citygml.CitygmlVersion

configure {

    opendrive2roadspaces {
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

    roadspaces2citygml {
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
    }

    citygmlReaderWriter {
        writeVersions = setOf(CitygmlVersion.V2_0, CitygmlVersion.V3_0)
    }
}
