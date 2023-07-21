# Changelog

## [1.3.0] - 2023-03-06
### Added
- validity checks according to [article](https://doi.org/10.5194/isprs-annals-X-4-W2-2022-257-2022)
- JSON reports
- compressed dataset writing
- OpenDRIVE cropping and offsetting
- CityGML code lists #21
- CityGML3 traffic direction #22

### Removed
- kscript due to complexity (conventional CLI is easier)

## [1.2.2] - 2021-10-14
### Fixed
- stop breaking for a specific parameter combination for road objects

## [1.2.1] - 2021-08-25
### Added
- added Docker support for deployment
- creation of LOD2 building models by estimating semantics (wall, roof, ground surface) based on normal vectors


## [1.2.0] - 2021-05-18
### Changed
- kscripts as main interface for editing recipes


## [1.1.5] - 2021-01-17
### Added
- added [ktlint](https://ktlint.github.io)

### Changed
- enhanced documentation #4
- more robust handling of outline elements with mixed height entries (zero & non-zero) #5
- updated dependencies

### Fixed
- stabilized concurrent processing

## [1.1.4] - 2020-12-04
### Added
- added export of absolute rotation angles

### Fixed
- more robust handling of plan view geometries
- bug fixing of ETA calculations


## [1.1.3] - 2020-10-13
### Added
- added handling of OpenDRIVE datasets with curve geometries (plan view) of length zero

### Fixed
- fixed bug of multiply used ids


## [1.1.2] - 2020-10-11
### Added
- added transformation of road markings (#1)
- added more types of filler surfaces in CityGML to close the road surface (#2)
- added generation of stable CityGML IDs (#3)
- implemented lane topology of OpenDRIVE
- implemented road shapes of OpenDRIVE
- added [concurrent](https://kotlinlang.org/docs/reference/coroutines-overview.html) processing of elements of a model (experimental)

### Changed
- updated to [Kotlin 1.4](https://kotlinlang.org/docs/reference/whatsnew14.html)

### Fixed
- more robust handling of OpenDRIVE variations
