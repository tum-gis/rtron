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


## Batch Processing

Usually the transformation parameters are similar per project.
To simplify parametrization for batch processing transformations, r:trån supports nested configurations:
```bash
tree ./opendrive-datasets 
  ./opendrive-datasets
  ├── asam.net
  │   ├── configuration.kts
  │   ├── Ex_Line-Spiral-Arc.xodr
  │   └── UC_ParamPoly3.xodr
  ├── configuration.kts
  └── opendrive.org
      ├── configuration.kts
      └── CrossingComplex8Course.xodr
```

A parameter in ``./opendrive-datasets/asam.net/configuration.kts`` will overwrite the equally named parameter in ``./opendrive-datasets/configuration.kts``.
All parameters provided in ``./opendrive-datasets/configuration.kts`` will also apply to the subdirectories, in case there is no parameter overwriting.
Default values are used, if no parameters are provided.

## Java Virtual Machine (JVM)

If larger OpenDRIVE datasets are to be transformed, the JVM should be provided with enough heap space:

```bash
java -Xmx50G -jar rtron-*.jar ./opendrive-datasets ./citygml-datasets
```
