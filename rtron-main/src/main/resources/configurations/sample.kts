import io.rtron.main.project.configuration.configure

configure {

    opendrive2roadspaces {
        tolerance = 1E-7
        attributesPrefix = "opendrive_"
        crsEpsg = 32632

        offsetX = 0.0
        offsetY = 0.0
        offsetZ = 0.0
    }

    roadspaces2citygml {
        gmlIdPrefix = "UUID_"
        identifierAttributesPrefix = "identifier_"
        flattenGenericAttributeSets = true
        discretizationStepSize = 0.5
        sweepDiscretizationStepSize = 0.3
        circleSlices = 12
        generateRandomGeometryIds = true
    }
}
