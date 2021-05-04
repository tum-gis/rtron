import io.rtron.main.project.configuration.configure

/**
 * The German Aerospace Center (DLR) provides an OpenDRIVE dataset of the inner-city ring road of Brunswick,
 * which can be downloaded here: https://zenodo.org/record/4043193
 *
 * This is a configuration file for their OpenDRIVE dataset.
 * To try it out, rename the file to configuration.kts and place it in the same directory as the dataset.
 */

configure {

    opendrive2roadspaces {
        crsEpsg = 32632
        offsetX = 604763.0
        offsetY = 5792795.0
    }

    roadspaces2citygml {
        discretizationStepSize = 0.5
        flattenGenericAttributeSets = true
    }
}
