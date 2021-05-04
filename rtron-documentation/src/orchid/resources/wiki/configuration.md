---
---

# Configuring r:trån

## Transformation

The transformation of models is usually dependent on a set of parameters.
r:trån can be configured by adding a script named ``configuration.kts`` into the directory of your source model:
```kotlin
import io.rtron.main.project.configuration.configure

configure {
    // parameter configuration of the OpenDRIVE->RoadSpaces transformer
    opendrive2roadspaces {
        crsEpsg = 32632 // EPSG of the coordinate reference system required for GIS applications
        offsetX = 0.0 // offset by which the model is translated along the x axis
    }
    // parameter configuration of the RoadSpaces->CityGML transformer
    roadspaces2citygml {
        discretizationStepSize = 0.5 // step size, which is used to discretize continuous functions
    }
}
```
For example, the transformation requires an [EPSG code](https://de.wikipedia.org/wiki/European_Petroleum_Survey_Group_Geodesy) which defines the coordinate reference system and cannot be directly derived from the OpenDRIVE dataset.
A complete sample configuration and further examples can be found in the [configuration folder](https://github.com/tum-gis/rtron/tree/master/rtron-main/src/main/resources/configurations) within the repository.

This script is an [internal DSL](https://en.wikipedia.org/wiki/Domain-specific_language) of Kotlin which guarantees type-safety.
Moreover, a DSL is suitable for describing transformation recipes and complex configurations, such as model mapping rules.
If you want to edit the configuration, [IntelliJ](https://www.jetbrains.com/idea/) is recommended as it has the best support for Kotlin.

## Transformation to CityGML 3.0

To transform an OpenDRIVE dataset to CityGML 3.0, download at least version 1.1.6 at the [releases section](https://github.com/tum-gis/rtron/releases) or checkout the development branch.
The configuration of the transformation is similar, but has the following additional parameters:
```kotlin
import io.rtron.main.project.configuration.configure
import io.rtron.readerwriter.citygml.CitygmlVersion // don't forget to include the versions

configure {
    opendrive2roadspaces {
        crsEpsg = 32632
    }
    roadspaces2citygml {
        // if true, only classes are populated that are also available in CityGML2
        mappingBackwardsCompatibility = false
    }
    // parameter configuration for writing the CityGML dataset
    citygmlReaderWriter {
        // set a single or multiple CityGML versions
        writeVersions = setOf(CitygmlVersion.V2_0, CitygmlVersion.V3_0)
    }
}
```

## Batch Processing

Usually the transformation parameters are similar per project.
To simplify parametrization for batch processing transformations, r:trån supports nested configurations:
```bash
tree ./input-datasets 
  ./input-datasets
  ├── 3d-mapping.de
  │   ├── configuration.kts
  │   ├── Ingolstadt_City_Hall.xodr
  │   └── Ingolstadt_Intersection.xodr
  ├── asam.net
  │   ├── Ex_Line-Spiral-Arc.xodr
  │   └── UC_ParamPoly3.xodr
  └── configuration.kts
```

A parameter in ``./input-datasets/3d-mapping.de/configuration.kts`` will overwrite the equally named parameter in ``./input-datasets/configuration.kts``.
All parameters provided in ``./input-datasets/configuration.kts`` will also apply to the subdirectories, in case there is no parameter overwriting.
Default values are used, if no parameters are provided.

## Java Virtual Machine (JVM)

If larger OpenDRIVE datasets are to be transformed, the JVM should be provided with enough heap space:

```bash
java -Xmx50G -jar rtron-*.jar ./input-datasets ./output-datasets
```
