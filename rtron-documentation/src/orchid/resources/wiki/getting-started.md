---
---

# Getting Started

## Datasets

Download some sample OpenDRIVE datasets of the city of Ingolstadt from the company [3D Mapping Solutions](https://www.3d-mapping.de/en/customer-area/demo-data) (initial registration required).
Additionally, [awesome-openx](https://github.com/b-schwab/awesome-openx#datasets) provides a list of further OpenDRIVE datasets.

## Prerequisites

r:trån requires JDK 11 or later versions.

If you are on Windows, install it with [choco](https://chocolatey.org/packages/openjdk11) by running:
```bash
choco install openjdk11
```

If you are on a Debian-based Linux, install the JDK by running:
```bash
sudo apt install openjdk-11-jre
```

If you are on macOS, install it with [homebrew](https://github.com/AdoptOpenJDK/homebrew-openjdk) by running:
```bash
brew tap AdoptOpenJDK/openjdk
brew cask install adoptopenjdk11
```

## Installation

Download the executable jar at the [releases section](https://github.com/tum-gis/rtron/releases) or build r:trån yourself:
```bash
git clone https://github.com/tum-gis/rtron.git
cd rtron

./gradlew shadowJar # build the uber-jar
cd rtron-cli/build/libs
```

## Usage

Start the transformations by running:
```bash
java -jar rtron.jar ./input-datasets ./output-datasets
```


## Batch Processing

Assuming there are multiple OpenDRIVE datasets contained in the input path, for example like this:
```bash
tree ./input-datasets
  ./input-datasets
  ├── 3d-mapping.de
  │   ├── Ingolstadt_City_Hall.xodr
  │   └── Ingolstadt_Intersection.xodr
  └── asam.net
      └── Ex_Line-Spiral-Arc.xodr
```
r:trån then recursively iterates over all models and generates the respective CityGML datasets.
The directory structure is preserved and report logs are added:

```bash
java -jar rtron-*.jar ./input-datasets ./output-datasets

tree ./output-datasets
  ./output-datasets
  ├── 3d-mapping.de
  │   ├── Ingolstadt_City_Hall
  │   │   ├── Ingolstadt_City_Hall.gml
  │   │   └── report.log
  │   └── Ingolstadt_Intersection
  │       ├── Ingolstadt_Intersection.gml
  │       └── report.log
  ├── asam.net
  │   └── Ex_Line-Spiral-Arc
  │       ├── Ex_Line-Spiral-Arc.gml
  │       └── report.log
  └── general.log
```
