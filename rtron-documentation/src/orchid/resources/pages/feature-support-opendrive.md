---
title: Feature Support of OpenDRIVE
---

# Feature Support of OpenDRIVE

In the following a list of OpenDRIVE features supported by r:trån is provided.
The new Transportation module of CityGML 3.0 will enable a range of new functions including the representation of traffic spaces together with lane topologies [[1](https://doi.org/https://doi.org/10.3390/ijgi9100603), [2](https://doi.org/10.1007/s41064-020-00095-z)].

### Geometry

- straight lines 🟢
- spirals 🟢
- arcs 🟢
- cubic polynomials 🔴 (deprecated anyway)
- parametric cubic polynomials 🟢

### Roads

- road linkage 🟡 (used for adding filler surfaces)
- road type 🟢
- methods of elevation 🟢
  - road elevation 🟢
  - super elevation 🟢
  - road shape 🟢
- road surface 🔴

### Lanes

- lane sections 🟢
- lane offset 🟢
- lane linkage 🟡 (used for adding filler surfaces)
- lane properties 🟢
- road markings 🟢

### Junctions

- incoming roads 🟡 (used for adding filler surfaces)
- connecting roads 🟡 (used for adding filler surfaces)
- road surface 🔴
- virtual junctions 🔴
- junction groups 🔴

### Objects

- repeating objects 🟡
- object outlines 🟢
  - corner roads 🟢
  - corner locals 🟢
- object material 🟢
- lane validity for objects 🔴
- access rules to parking spaces 🔴
- object marking 🔴
- object border 🔴
- object reference 🔴
- tunnels 🔴
- bridges 🔴

### Signals

- lane validity 🔴
- signal dependency 🔴
- links between signals and objects 🔴
- signal positioning 🟢
- reuse of signal information 🔴
- controllers 🔴

### Railroads

- railroad tracks 🔴
- switches 🔴
- stations 🔴


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
