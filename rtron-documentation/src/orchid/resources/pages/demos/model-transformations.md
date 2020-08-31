---
title: Model Transformations
---

# Model Transformations

Create model derivatives using [FME workbenches](https://www.safe.com/fme/):

![FME Workbench](/assets/media/demos/model-transformations-fme-workbench.png)

This workbench reads a transformed OpenDRIVE dataset and creates:
* a GeoJSON file in a reprojected coordinate system
* a FBX file in a local coordinate system
* and a DWG file for AutoCAD containing only the center lines of the lanes

FME supports a [magnitude of formats](https://www.safe.com/fme/formats-matrix/) including glTF, OBJ and Unreal Datasmith.
Moreover, FME provides a [variety of transformers](https://www.safe.com/transformers/) that can be combined to realize feature manipulation tasks.

## References

- [Webinar](https://www.safe.com/webinars/your-data-in-unreal-how-to-bring-your-data-into-real-time-environments/) on how to bring various models into the Unreal Game engine with FME
- [Article](https://safesoftware.cloud.answerhub.com/articles/54027/creating-and-using-geometry-instances.html) on how to swap primitive geometries with HD model assets
