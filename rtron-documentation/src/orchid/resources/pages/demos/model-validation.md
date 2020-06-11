---
title: Model Validation
---

# Model Validation

## Transformation Logs

OpenDRIVE datasets can reveal different anomalies, which may be divided into two categories.
1. If the anomaly causes the removal of an object, a warning message is issued.
2. If the anomaly can be overcome, an information message is issued.

```text
2020-06-01T10:41:57,495 INFO  RoadspaceObjectIdentifier(roadspaceObjectId=4017180, roadspaceId=1397000): Removing consecutively following side duplicates of the form (…, A, B, A, …).
2020-06-01T10:41:57,674 INFO  RoadspaceObjectIdentifier(roadspaceObjectId=4017016, roadspaceId=1418000): Removing at least one vertex due to linear redundancy.
```

## Comparing OpenDRIVE with OpenStreetMap using FME

This workbench takes tree features of OpenStreetMap and finds the corresponding tree within the OpenDRIVE dataset.
![FME Workbench](/assets/media/demos/model-validation-fme-workbench.png)

Trees with a corresponding neighbor below a certain distance threshold are colored green and the distance measurement is added to the tree as attribute.
![FME Workbench](/assets/media/demos/model-validation-fme-inspector.png)
