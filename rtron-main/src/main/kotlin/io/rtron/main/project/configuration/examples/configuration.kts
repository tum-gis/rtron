import io.rtron.main.project.configuration.configure

configure {

    opendrive2roadspaces {
        tolerance = 1E-7
        attributesPrefix = "opendrive_"
        crsEpsg = 32632
    }

    roadspaces2citygml {
        discretizationStepSize = 0.5
        sweepDiscretizationStepSize = 0.3
        circleSlices = 12
        flattenGenericAttributeSets = true
    }
}
