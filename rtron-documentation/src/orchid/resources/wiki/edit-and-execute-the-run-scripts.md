---
---

# Edit and Execute the Run Scripts

Download some sample OpenDRIVE datasets of the city of Ingolstadt from the company [3D Mapping Solutions](https://www.3d-mapping.de/en/customer-area/demo-data) (initial registration required).
Additionally, [awesome-openx](https://github.com/b-schwab/awesome-openx#datasets) provides a list of further OpenDRIVE datasets.

## Getting Started

First, clone the repository to your local machine:

```bash
git clone https://github.com/tum-gis/rtron.git
```

To customize and adjust the run scripts, any editor will work.
However, an IDE offers convenient suggestions and auto-completion.

Thus, install the community edition of [IntelliJ](https://www.jetbrains.com/idea/download) and double check that IntelliJ is configured correctly:
- JVM of version 11 for Gradle
    - Navigate to the setting menu: `Menu` ➔ `File` ➔ `Settings…` ➔ `Build, Execution, Deployment` ➔ `Build Tools` ➔ `Gradle`
    - If `Gradle JVM` is not set to a JVM of version 11, download e.g. `AdoptOpenJDK (OpenJ9) 11.0.11`
- Kotlin plugin of at least version *-1.5.0
    - Navigate to the setting menu: `Menu` ➔ `File` ➔ `Settings…` ➔ `Plugins`
    - Update the plugin, if possible

## Usage

Open the cloned r:trån project and grab a coffee.
Then, navigate to the script [rtron-main/src/main/resources/scripts/convert-opendrive-to-citygml2-simple.kts](https://github.com/tum-gis/rtron/blob/main/rtron-main/src/main/resources/scripts/convert-opendrive-to-citygml2-simple.kts) and execute it by hitting `Menu` ➔ `Run` ➔ `Run…` (or Alt+Shift+F10):

```kotlin
import io.rtron.main.project.processAllFiles
import io.rtron.readerwriter.citygml.CitygmlVersion

/**
 * This function iterates over all files contained in the input directory that have the
 * extension "xodr".
 */
processAllFiles(
    inInputDirectory = "/path/to/input-datasets", // TODO: adjust path
    withExtension = "xodr",
    toOutputDirectory = "/path/to/output-datasets" // TODO: adjust path
)
{
    // Within this block the transformations can be defined by the user. For example:

    // 1. Read the OpenDRIVE dataset into memory:
    val opendriveModel = readOpendriveModel(inputFilePath)

    // 2. Transform the OpenDRIVE model to an intermediary representation (the RoadSpaces model):
    val roadspacesModel = transformOpendrive2Roadspaces(opendriveModel) {
        // Within this blocks, the transformation is parametrized:

        // EPSG code of the coordinate reference system (needed by GIS applications)
        crsEpsg = 32632
    }

    // 3. Transform the RoadSpaces model to a CityGML model:
    val citygmlModel = transformRoadspaces2Citygml(roadspacesModel) {
        // true, if nested attribute lists shall be flattened out
        flattenGenericAttributeSets = true

        // distance between each discretization step for curves and surfaces
        discretizationStepSize = 0.5
    }

    // 4. Write the CityGML model to the output directory:
    writeCitygmlModel(citygmlModel) {

        // set the CityGML versions for writing
        versions = setOf(CitygmlVersion.V2_0)
    }
}
```

After the execution is completed, the directory ``/path/to/output-datasets`` should contain the converted CityGML2 datasets.
A list of all configuration options can be found in the [convert-opendrive-to-citygml2-full.kts](https://github.com/tum-gis/rtron/blob/main/rtron-main/src/main/resources/scripts/convert-opendrive-to-citygml2-full.kts) script.

## FAQs

### Conversion to CityGML 3.0

Version 3.0 of the [CityGML](https://www.ogc.org/standards/citygml) standard is currently being finalized, with the conceptual data model available [here](https://github.com/opengeospatial/CityGML-3.0CM).
To convert OpenDRIVE datasets to CityGML3.0, checkout the [convert-opendrive-to-citygml3.kts](https://github.com/tum-gis/rtron/blob/main/rtron-main/src/main/resources/scripts/convert-opendrive-to-citygml3.kts) script.


### Java Virtual Machine (JVM)

If larger OpenDRIVE datasets are to be transformed, the JVM should be provided with enough heap space.
This can be achieved by adding the argument `-Xmx50G` (for 50GB) as a VM Option in IntelliJ's Run Configuration.
