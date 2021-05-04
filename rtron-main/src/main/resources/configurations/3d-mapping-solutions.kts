import io.rtron.main.project.configuration.configure

/**
 * The company 3D Mapping Solutions provides sample OpenDRIVE datasets, which can be downloaded here (after initial registration):
 * https://www.3d-mapping.de/en/customer-area/demo-data/
 *
 * This is a configuration file for their OpenDRIVE datasets of the city of Ingolstadt.
 * To try it out, rename the file to configuration.kts and place it in the same directory as the dataset.
 *
 * Besides, this configuration is also used in the research project SAVe (https://save-in.digital).
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
