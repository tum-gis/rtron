---
---

# Configuring r:trån



## Transformation

The transformation of models is usually dependent on a set of parameters.
r:trån can be configured by adding a script named ``configuration.kts`` into the directory of your source model:
```kotlin
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
```
This script is an [internal DSL](https://en.wikipedia.org/wiki/Domain-specific_language) of Kotlin which guarantees type-safety.
Moreover, a DSL is suitable for describing transformation recipes and complex configurations, such as model mapping rules.

If you want to edit the configuration, [IntelliJ](https://www.jetbrains.com/idea/) is recommended as it has the best support for Kotlin.
An example configuration script can be found at ``rtron-main/src/main/kotlin/io/rtron/main/project/configuration/examples/configuration.kts``.


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
