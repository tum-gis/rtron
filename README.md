<p align="center"><a href="https://rtron.io" target="_blank" rel="noopener noreferrer"><img width="500" src="https://raw.githubusercontent.com/tum-gis/rtron-site/main/public/logo.png" alt="rtron logo"></a></p>

<p align="center">
    <em>a road space model transformer library for OpenDRIVE, CityGML and beyond</em>
    <br />
    <a href="https://rtron.io">View Demos</a>
    ·
    <a href="https://github.com/tum-gis/rtron/issues">Report Bug</a>
    ·
    <a href="https://github.com/tum-gis/rtron/issues">Request Feature</a>
</p>

<p align="center">
    <a href="https://search.maven.org/search?q=io.rtron" title="Latest Release"><img src="https://img.shields.io/maven-central/v/io.rtron/rtron-main?style=for-the-badge"></a>
    <a href="https://github.com/tum-gis/rtron/actions/workflows/build-rtron.yml" title="Build Status"><img src="https://img.shields.io/github/actions/workflow/status/tum-gis/rtron/build-rtron.yml?branch=main&style=for-the-badge"></a>
    <a href="https://github.com/tum-gis/rtron/issues" title="Open Issues"><img src="https://img.shields.io/github/issues/tum-gis/rtron?style=for-the-badge&logo=Github"></a>
    <a title="Language"><img src="https://img.shields.io/github/languages/top/tum-gis/rtron.svg?style=for-the-badge&logo=Kotlin&logoColor=white"></a>
    <a href="./LICENSE" title="License"><img src="https://img.shields.io/github/license/tum-gis/rtron.svg?style=for-the-badge&logo=Apache"></a>
</p>

![rtron preview](https://raw.githubusercontent.com/tum-gis/rtron-site/main/public/rtron/rtron-preview.png)

r:trån reads road network models in [OpenDRIVE](https://www.asam.net/standards/detail/opendrive) and transforms them to the virtual 3D city model standard [CityGML](https://www.opengeospatial.org/standards/citygml).

This enables you to

* [inspect](https://rtron.io/demos/model-inspection) your spatio-semantic road space models
* conduct further [model transformations](https://rtron.io/demos/model-transformations) with tools like [FME](https://www.safe.com/fme/)
* perform geospatial analyses on the [3D City Database](https://rtron.io/demos/3dcitydb)
* deploy [virtual globes](https://rtron.io/demos/web-map)
* load your models into a [desktop GIS](https://rtron.io/demos/desktop-gis)
* [compare and validate](https://rtron.io/demos/model-validation) your models with models from other data sources

## :inbox_tray: Datasets

Download some sample OpenDRIVE datasets of the city of Ingolstadt from the company [3D Mapping Solutions](https://www.3d-mapping.de/en/customer-area/demo-data) (initial registration required).
Additionally, [awesome-openx](https://github.com/b-schwab/awesome-openx#datasets) provides a list of further OpenDRIVE datasets.

## :rocket: Usage

In order to use r:trån you need JDK 11 or later.
Download the prebuilt JAR executable from the [releases section](https://github.com/tum-gis/rtron/releases/latest) and make sure that you have at least a JVM 11.
Run r:trån to ...

```bash
# … validate OpenDRIVE datasets
java -jar rtron.jar validate-opendrive ./input-opendrive ./output-reports

# … transform OpenDRIVE datasets to CityGML
java -jar rtron.jar opendrive-to-citygml ./input-opendrive ./output-citygml
```

R:trån recursively iterates over your OpenDRIVE input datasets and creates the same directory structure for the output folder.

## :construction_worker: Building

Clone the repo and let gradle build it:

```bash
./gradlew shadowJar # build the uber-jar
cd rtron-cli/build/libs
java -jar rtron-*.jar
```

You're good to go :muscle:


## :hammer_and_wrench: Contributing

r:trån was developed so that everyone can benefit from spatio-semantic road space models.
Therefore, bug fixes, issue reports and contributions are greatly appreciated.

## :mortar_board: Research

The software r:trån can be cited using the [![DOI](https://zenodo.org/badge/258142903.svg)](https://zenodo.org/badge/latestdoi/258142903).

If you use this software, please cite one of the supplemental research articles:
- [Spatio-Semantic Road Space Modeling for Vehicle-Pedestrian Simulation to Test Automated Driving Systems](https://doi.org/10.3390/su12093799)
- [Validation of Parametric OpenDRIVE Road Space Models](https://doi.org/10.5194/isprs-annals-X-4-W2-2022-257-2022)

These papers may also be of interest:
* [Detailed Streetspace Modelling for Multiple Applications: Discussions on the Proposed CityGML 3.0 Transportation Model](https://doi.org/https://doi.org/10.3390/ijgi9100603)
* [Requirement Analysis of 3D Road Space Models for Automated Driving](https://doi.org/10.5194/isprs-annals-IV-4-W8-99-2019)
* [CityGML and the streets of New York - A proposal for detailed street space modelling](https://doi.org/10.5194/isprs-annals-IV-4-W5-9-2017)

## :memo: License

r:trån is distributed under the Apache License 2.0. See [LICENSE](LICENSE) for more information.

## :handshake: Thanks

- [Lutz Morich](https://www.linkedin.com/in/lutz-morich-in/) and [AUDI AG](https://github.com/audi) for providing an awesome work environment within *SAVe*
- Prof. [Thomas H. Kolbe](https://www.asg.ed.tum.de/en/gis/our-team/staff/prof-thomas-h-kolbe/), [Bruno Willenborg](https://www.asg.ed.tum.de/en/gis/our-team/staff/bruno-willenborg/) and [Christof Beil](https://www.asg.ed.tum.de/en/gis/our-team/staff/christof-beil/) for support and feedback
- [Claus Nagel](https://github.com/clausnagel) for [citygml4j](https://github.com/citygml4j/citygml4j)
- [JetBrains](https://github.com/JetBrains) for Kotlin and their top-notch IDEs
