/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.opendrive.road.objects

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.Pose3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.model.opendrive.common.DataQuality
import io.rtron.model.opendrive.common.EObjectType
import io.rtron.model.opendrive.common.Include
import io.rtron.model.opendrive.common.UserData
import io.rtron.std.ContextMessage


data class RoadObjectsObject(
        var repeat: RoadObjectsObjectRepeat = RoadObjectsObjectRepeat(),
        // outline is deprecated
        var outlines: RoadObjectsObjectOutlines = RoadObjectsObjectOutlines(),
        var material: List<RoadObjectsObjectMaterial> = listOf(),
        var validity: List<RoadObjectsObjectLaneValidity> = listOf(),
        var parkingSpace: RoadObjectsObjectParkingSpace = RoadObjectsObjectParkingSpace(),
        var markings: RoadObjectsObjectMarkings = RoadObjectsObjectMarkings(),
        var borders: RoadObjectsObjectBorders = RoadObjectsObjectBorders(),

        var userData: List<UserData> = listOf(),
        var include: List<Include> = listOf(),
        var dataQuality: DataQuality = DataQuality(),

        var type: EObjectType = EObjectType.NONE,
        var subtype: String = "",
        var dynamic: Boolean = false,
        var name: String = "",
        var id: String = "",

        var s: Double = Double.NaN,
        var t: Double = Double.NaN,
        var zOffset: Double = 0.0,

        var validLength: Double = Double.NaN,
        var orientation: String = "",
        var hdg: Double = 0.0,
        var pitch: Double = 0.0,
        var roll: Double = 0.0,

        var height: Double = Double.NaN,

        // cuboid
        var length: Double = Double.NaN,
        var width: Double = Double.NaN,
        // cylinder
        var radius: Double = Double.NaN
) {
    // Properties and Initializers
    val curveRelativePosition get() = CurveRelativeVector3D(s, t, zOffset)

    /** position of the object relative to the point on the road reference line */
    val referenceLinePointRelativePosition get() = Vector3D(0.0, t, zOffset)

    /** rotation of the object relative to the rotation on the road reference line */
    val referenceLinePointRelativeRotation get() = Rotation3D(hdg, pitch, roll)

    /** pose of the object relative to the pose on the road reference line */
    val referenceLinePointRelativePose
        get() =
            Pose3D(referenceLinePointRelativePosition, referenceLinePointRelativeRotation)

    // Methods
    fun getPolyhedronsDefinedByRoadCorners(): List<RoadObjectsObjectOutlinesOutline> =
        outlines.getPolyhedronsDefinedByRoadCorners()

    fun getPolyhedronsDefinedByLocalCorners(): List<RoadObjectsObjectOutlinesOutline> =
        outlines.getPolyhedronsDefinedByLocalCorners()

    fun getLinearRingsDefinedByRoadCorners(): List<RoadObjectsObjectOutlinesOutline> =
        outlines.getLinearRingsDefinedByRoadCorners()

    fun getLinearRingsDefinedByLocalCorners(): List<RoadObjectsObjectOutlinesOutline> =
        outlines.getLinearRingsDefinedByLocalCorners()


    /** Returns true, if the provided geometry information correspond to a cuboid. */
    fun isCuboid() = !length.isNaN() && length > 0.0 && !width.isNaN() && width > 0.0 &&
            !height.isNaN() && height > 0.0 && !outlines.containsGeometries()

    /** Returns true, if the provided geometry information correspond to a rectangle. */
    fun isRectangle() = !length.isNaN() && length > 0.0 && !width.isNaN() && width > 0.0 &&
            (height.isNaN() || height == 0.0) && !outlines.containsGeometries()

    /** Returns true, if the provided geometry information correspond to a cylinder. */
    fun isCylinder() = !radius.isNaN() && !height.isNaN() && radius > 0.0 && height > 0.0

    /** Returns true, if the provided geometry information correspond to a circle. */
    fun isCircle() = !radius.isNaN() && radius > 0.0 && (height.isNaN() || height == 0.0)

    /** Returns true, if the provided geometry information correspond to a point. */
    fun isPoint() = !isCuboid() && !isRectangle() && !isCylinder() && !outlines.containsGeometries() && !repeat.isSet()

    fun isProcessable(): Result<ContextMessage<Boolean>, IllegalStateException> {

        if (outlines.outline.any { it.isPolyhedron() && !it.isPolyhedronUniquelyDefined() })
            return Result.error(IllegalStateException("Road object has mixed outline definitions. This is " +
                    "not allowed according to the standard."))

        val infos = mutableListOf<String>()

        if (length < 0.0) infos += "Road object has a negative length ($length)."
        if (width < 0.0) infos += "Road object has a negative width ($width)."
        if (height < 0.0) infos += "Road object has a negative height ($height)."

        if (!height.isNaN() && height == 0.0 && outlines.containsPolyhedrons())
            infos += "Road object contains a polyhedron with non-zero height, but the height of the road " +
                    "object element is $height."

        return Result.success(ContextMessage(true, infos))
    }
}
