import io.rtron.main.project.configuration.configure

/**
 * In the research project SAVe (https://save-in.digital) this configuration is mostly used.
 */

configure {
    opendrive2roadspaces {
        crsEpsg = 32632
    }

    roadspaces2citygml {
        discretizationStepSize = 0.5
        flattenGenericAttributeSets = true
    }
}
