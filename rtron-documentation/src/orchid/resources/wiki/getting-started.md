---
---

# Getting Started


## Preliminaries

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

## Usage

Download the executable jar at the [releases section](https://github.com/tum-gis/rtron/releases) or build it yourself:
```bash
git clone https://github.com/tum-gis/rtron.git
cd rtron

./gradlew shadowJar # build the uber-jar
cd rtron-cli/build/libs
```

Let it run:
```bash
java -jar rtron-*.jar
  Usage: rtron [OPTIONS] INPUTPATH OUTPUTPATH
  
    r:trån transforms road networks described in OpenDRIVE into the virtual 3D
    city model standard CityGML.
  
  Options:
    --version       Show the version and exit
    --no-recursive  Do not search recursively for input files in given
                    input directory
    --parallel      Run processing in parallel
    -c, --clean     Clean output directory by deleting its
                    current content before starting
    -h, --help      Show this message and exit
  
  Arguments:
    INPUTPATH   Path to the directory containing OpenDRIVE datasets
    OUTPUTPATH  Path to the output directory into which the
                transformed CityGML models are written
```

## Batch Processing

Assuming there are multiple OpenDRIVE datasets contained in the input path, for example like this:
```bash
tree ./opendrive-datasets
  ./opendrive-datasets
  ├── asam.net
  │   ├── Ex_Line-Spiral-Arc.xodr
  │   └── UC_ParamPoly3.xodr
  └── opendrive.org
      └── CrossingComplex8Course.xodr
```
r:trån then recursively iterates over all models and generates the respective CityGML datasets.
The directory structure is preserved and report logs are added:

```bash
java -jar rtron-*.jar ./opendrive-datasets ./citygml-datasets

tree ./citygml-datasets
  ./citygml-datasets
  ├── asam.net
  │   ├── Ex_Line-Spiral-Arc
  │   │   ├── Ex_Line-Spiral-Arc.gml
  │   │   └── report.log
  │   └── UC_ParamPoly3
  │       ├── report.log
  │       └── UC_ParamPoly3.gml
  ├── general.log
  └── opendrive.org
      └── CrossingComplex8Course
          ├── CrossingComplex8Course.gml
          └── report.log
```
