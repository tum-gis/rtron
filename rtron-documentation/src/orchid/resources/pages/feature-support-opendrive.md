---
title: Feature Support of OpenDRIVE
---

# Feature Support of OpenDRIVE

In the following a list of OpenDRIVE features supported by r:trån is provided.
The new Transportation module of CityGML 3.0 will enable a range of new functions including the representation of traffic spaces together with detailed lane topologies [[1](https://doi.org/https://doi.org/10.3390/ijgi9100603), [2](https://doi.org/10.1007/s41064-020-00095-z)].

Currently, there is tested support for OpenDRIVE version 1.4, although other versions can also be read and processed experimentally.
Full support for further versions can be conveniently added due to the [architecture](architecture) of r:trån.

### Geometry

- straight lines <span style="color: var(--green)">⬤</span>
- spirals <span style="color: var(--green)">⬤</span>
- arcs <span style="color: var(--green)">⬤</span>
- cubic polynomials <span style="color: var(--red)">⬤</span> (deprecated anyway)
- parametric cubic polynomials <span style="color: var(--green)">⬤</span>

### Roads

- road linkage <span style="color: var(--orange)">⬤</span> (used for adding filler surfaces)
- road type <span style="color: var(--green)">⬤</span>
- methods of elevation <span style="color: var(--green)">⬤</span>
- road elevation <span style="color: var(--green)">⬤</span>
- super elevation <span style="color: var(--green)">⬤</span>
- road shape <span style="color: var(--green)">⬤</span>
- road surface <span style="color: var(--red)">⬤</span>

### Lanes

- lane sections <span style="color: var(--green)">⬤</span>
- lane offset <span style="color: var(--green)">⬤</span>
- lane linkage <span style="color: var(--orange)">⬤</span> (used for adding filler surfaces)
- lane properties <span style="color: var(--green)">⬤</span>
- road markings <span style="color: var(--green)">⬤</span>

### Junctions

- incoming roads <span style="color: var(--orange)">⬤</span> (used for adding filler surfaces)
- connecting roads <span style="color: var(--orange)">⬤</span> (used for adding filler surfaces)
- road surface <span style="color: var(--red)">⬤</span>
- virtual junctions <span style="color: var(--red)">⬤</span>
- junction groups <span style="color: var(--red)">⬤</span>

### Objects

Depending on the availability and characteristic of the parametric OpenDRIVE geometries, [B-Rep](https://en.wikipedia.org/wiki/Boundary_representation) representations are generated.
Thereby, several geometry corrections are applied (removal of invalid vertices, consecutively following vertex duplicates, ...) and then mapped onto the rich geometry model of [GML](https://www.ogc.org/standards/gml).

- repeating objects <span style="color: var(--green)">⬤</span>
- object outlines <span style="color: var(--green)">⬤</span>
  - corner roads <span style="color: var(--green)">⬤</span>
  - corner locals <span style="color: var(--green)">⬤</span>
- object material <span style="color: var(--green)">⬤</span>
- lane validity for objects <span style="color: var(--red)">⬤</span>
- access rules to parking spaces <span style="color: var(--red)">⬤</span>
- object marking <span style="color: var(--red)">⬤</span>
- object border <span style="color: var(--red)">⬤</span>
- object reference <span style="color: var(--red)">⬤</span>
- tunnels <span style="color: var(--red)">⬤</span>
- bridges <span style="color: var(--red)">⬤</span>

### Signals

- lane validity <span style="color: var(--red)">⬤</span>
- signal dependency <span style="color: var(--red)">⬤</span>
- links between signals and objects <span style="color: var(--red)">⬤</span>
- signal positioning <span style="color: var(--green)">⬤</span>
- reuse of signal information <span style="color: var(--red)">⬤</span>
- controllers <span style="color: var(--red)">⬤</span>

### Railroads

- railroad tracks <span style="color: var(--red)">⬤</span>
- switches <span style="color: var(--red)">⬤</span>
- stations <span style="color: var(--red)">⬤</span>


## Datasets

Tools and applications often implement and interpret the OpenDRIVE standard in (slightly) different ways.
For this reason, r:trån is tested with the widest variety of OpenDRIVE datasets available.
This list currently includes datasets originating from the following organizations and implementations:

- Standardization organization: [Association of Standardization of Automation and Measuring Systems (ASAM)](https://www.asam.net/standards/detail/opendrive/)
- Surveying companies
    - [3D Mapping Solutions](https://www.3d-mapping.de/en/)
    - [Atlatec](https://www.atlatec.de/)
- Editors
    - [Road Network Editor (ROD)](https://www.mscsoftware.com/product/virtual-test-drive) of Vires
    - [RoadRunner](https://mathworks.com/products/roadrunner.html) of MathWorks
    - [Trian3DBuilder](https://trian3dbuilder.de/) of TrianGraphics
    - [ODDLOT](https://www.hlrs.de/solutions-services/service-portfolio/visualization/driving-simulator/oddlot/) of HLRS

A list of publicly available OpenDRIVE datasets can be found [here](https://github.com/b-schwab/awesome-openx#datasets).

## References

1. Beil, Christof; Ruhdorfer, Roland; Coduro, Theresa; Kolbe Thomas H.: [Detailed Streetspace Modelling for Multiple Applications: Discussions on the Proposed CityGML 3.0 Transportation Model](https://doi.org/https://doi.org/10.3390/ijgi9100603). ISPRS International Journal of Geo-Information 9 (10), 2020, 603.
2. Kutzner, Tatjana; Chaturvedi, Kanishk; Kolbe Thomas H.: [CityGML 3.0: New Functions Open Up New Applications](https://doi.org/10.1007/s41064-020-00095-z). PFG – Journal of Photogrammetry, Remote Sensing and Geoinformation Science, 2020, 19.
