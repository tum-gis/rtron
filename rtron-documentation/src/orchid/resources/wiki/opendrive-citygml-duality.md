---
---

# OpenDRIVE-CityGML Duality

The conceptual data models of OpenDRIVE and CityGML meet requirements from different application domains—automotive and 3D GIS [[1](https://doi.org/10.5194/isprs-annals-IV-4-W8-99-2019)].
However, challenges in one domain have already been addressed in the other.
Thus, the concept of the OpenDRIVE-CityGML Duality is being proposed:
With a consistent road space model in both OpenDRIVE and CityGML, the tools from both domains can be applied [[2](https://doi.org/10.3390/su12093799)].

<p style="text-align:center;">
    <img src="/assets/media/opendrive-citygml-duality.svg" alt="OpenDRIVE-CityGML Duality">
</p>

With transformation tools like [FME](/demos/model-transformations), application-specific target formats can be derived from CityGML datasets.
The dual representation enables the usage of generic attributes and Application Domain Extensions (ADEs) of CityGML [[3](https://doi.org/10.1186/s40965-018-0055-6)].

### References

1. Schwab, Benedikt; Kolbe, Thomas H.: [Requirement Analysis of 3D Road Space Models for Automated Driving](https://doi.org/10.5194/isprs-annals-IV-4-W8-99-2019). ISPRS Annals of Photogrammetry, Remote Sensing and Spatial Information Sciences IV-4/W8, 2019, 99-106.
2. Schwab, Benedikt; Beil, Christof; Kolbe Thomas H.: [Spatio-Semantic Road Space Modeling for Vehicle–Pedestrian Simulation to Test Automated Driving Systems](https://doi.org/10.3390/su12093799). Sustainability 12 (9), 2020, 3799.
3. Biljecki, Filip; Kumar, Kavisha; Nagel Claus: [CityGML Application Domain Extension (ADE): overview of developments](https://doi.org/10.1186/s40965-018-0055-6). Open Geospatial Data, Software and Standards 3 (13), 2018.
