/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rtron.model.opendrive.objects

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import arrow.optics.OpticsTarget
import arrow.optics.optics
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.Pose3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.model.opendrive.additions.identifier.AdditionalRoadObjectIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadObjectIdentifier
import io.rtron.model.opendrive.core.OpendriveElement

@optics([OpticsTarget.LENS, OpticsTarget.PRISM, OpticsTarget.OPTIONAL, OpticsTarget.DSL])
data class RoadObjectsObject(
    var repeat: List<RoadObjectsObjectRepeat> = emptyList(),
    // outline is deprecated
    var outlines: Option<RoadObjectsObjectOutlines> = None,
    var material: List<RoadObjectsObjectMaterial> = emptyList(),
    var validity: List<RoadObjectsObjectLaneValidity> = emptyList(),
    var parkingSpace: Option<RoadObjectsObjectParkingSpace> = None,
    var markings: Option<RoadObjectsObjectMarkings> = None,
    var borders: Option<RoadObjectsObjectBorders> = None,
    var surface: Option<RoadObjectsObjectSurface> = None,
    var dynamic: Option<Boolean> = None,
    var hdg: Option<Double> = None,
    var height: Option<Double> = None,
    var id: String = "",
    var length: Option<Double> = None,
    var name: Option<String> = None,
    var orientation: Option<EOrientation> = None,
    var perpToRoad: Option<Boolean> = None,
    var pitch: Option<Double> = None,
    var radius: Option<Double> = None,
    var roll: Option<Double> = None,
    var s: Double = Double.NaN,
    var subtype: Option<String> = None,
    var t: Double = Double.NaN,
    var type: Option<EObjectType> = None,
    var validLength: Option<Double> = None,
    var width: Option<Double> = None,
    var zOffset: Double = 0.0,
    override var additionalId: Option<RoadObjectIdentifier> = None,
) : OpendriveElement(), AdditionalRoadObjectIdentifier {
    // Validation Properties

    val heightValidated: Option<Double>
        get() = height.flatMap { if (it == 0.0) None else it.some() }

    // Properties and Initializers
    val curveRelativePosition get() = CurveRelativeVector3D(s, t, zOffset)

    /** position of the object relative to the point on the road reference line */
    val referenceLinePointRelativePosition get() = Vector3D(0.0, t, zOffset)

    /** rotation of the object relative to the rotation on the road reference line */
    val referenceLinePointRelativeRotation get() = Rotation3D.of(hdg, pitch, roll)

    /** pose of the object relative to the pose on the road reference line */
    val referenceLinePointRelativePose
        get() = Pose3D(referenceLinePointRelativePosition, referenceLinePointRelativeRotation)

    // Methods
    fun getPolyhedronsDefinedByRoadCorners(): List<RoadObjectsObjectOutlinesOutline> =
        outlines.fold({ emptyList() }, { it.getPolyhedronsDefinedByRoadCorners() })

    fun getPolyhedronsDefinedByLocalCorners(): List<RoadObjectsObjectOutlinesOutline> =
        outlines.fold({ emptyList() }, { it.getPolyhedronsDefinedByLocalCorners() })

    fun getLinearRingsDefinedByRoadCorners(): List<RoadObjectsObjectOutlinesOutline> =
        outlines.fold({ emptyList() }, { it.getLinearRingsDefinedByRoadCorners() })

    fun getLinearRingsDefinedByLocalCorners(): List<RoadObjectsObjectOutlinesOutline> =
        outlines.fold({ emptyList() }, { it.getLinearRingsDefinedByLocalCorners() })

    /** Returns true, if the provided geometry information correspond to a cuboid. */
    fun containsCuboid() = length.isSome() && width.isSome() && heightValidated.isSome()

    /** Returns true, if the provided geometry information correspond to a rectangle. */
    fun containsRectangle() = length.isSome() && width.isSome() && heightValidated.isNone()

    /** Returns true, if the provided geometry information correspond to a cylinder. */
    fun containsCylinder() = radius.isSome() && heightValidated.isSome()

    /** Returns true, if the provided geometry information correspond to a circle. */
    fun containsCircle() = radius.isSome() && heightValidated.isNone()

    companion object
}
