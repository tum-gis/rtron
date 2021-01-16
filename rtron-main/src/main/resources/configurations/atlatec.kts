import io.rtron.main.project.configuration.configure

/**
 * The company atlatec provides sample OpenDRIVE datasets, which can be downloaded here:
 * https://www.atlatec.de/getsampledata.html
 *
 * This is a configuration file for their San Francisco OpenDRIVE dataset.
 * To try it out, rename the file to sample.kts and place it in the same directory as the dataset.
 */

configure {

    opendrive2roadspaces {
        crsEpsg = 32610
        offsetX = 500000.0 + 52300.0
        offsetY = 4182400.0
    }

    roadspaces2citygml {
        discretizationStepSize = 0.5
        flattenGenericAttributeSets = true
    }
}
