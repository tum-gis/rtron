/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.opendrive.signal

import arrow.core.None
import arrow.core.Option
import arrow.optics.OpticsTarget
import arrow.optics.optics
import io.rtron.math.geometry.curved.threed.point.CurveRelativeVector3D
import io.rtron.math.geometry.euclidean.threed.Pose3D
import io.rtron.math.geometry.euclidean.threed.Rotation3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.model.opendrive.additions.identifier.AdditionalRoadSignalIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadSignalIdentifier
import io.rtron.model.opendrive.core.ECountryCode
import io.rtron.model.opendrive.core.EUnit
import io.rtron.model.opendrive.core.OpendriveElement
import io.rtron.model.opendrive.objects.EOrientation
import io.rtron.model.opendrive.objects.RoadObjectsObjectLaneValidity
import io.rtron.model.opendrive.objects.toRotation2D

@optics([OpticsTarget.LENS, OpticsTarget.PRISM, OpticsTarget.OPTIONAL, OpticsTarget.DSL])
data class RoadSignalsSignal(
    var validity: List<RoadObjectsObjectLaneValidity> = emptyList(),
    var dependency: List<RoadSignalsSignalDependency> = emptyList(),
    var reference: List<RoadSignalsSignalReference> = emptyList(),

    var positionInertial: Option<RoadSignalsSignalPositionInertial> = None,
    var positionRoad: Option<RoadSignalsSignalPositionRoad> = None,

    var country: Option<ECountryCode> = None,
    var countryRevision: Option<String> = None,
    var dynamic: Boolean = false,
    var height: Option<Double> = None,
    var hOffset: Option<Double> = None,
    var id: String = "",
    var name: Option<String> = None,
    var orientation: EOrientation = EOrientation.NONE,
    var pitch: Option<Double> = None,
    var roll: Option<Double> = None,
    var s: Double = Double.NaN,
    var subtype: String = "",
    var t: Double = Double.NaN,
    var text: Option<String> = None,
    var type: String = "",
    var unit: Option<EUnit> = None,
    var value: Option<Double> = None,
    var width: Option<Double> = None,
    var zOffset: Double = Double.NaN,

    override var additionalId: Option<RoadSignalIdentifier> = None
) : OpendriveElement(), AdditionalRoadSignalIdentifier {
    // Properties and Initializers
    val curveRelativePosition get() = CurveRelativeVector3D(s, t, zOffset)

    /** position of the object relative to the point on the road reference line */
    val referenceLinePointRelativePosition get() = Vector3D(0.0, t, zOffset)

    /** rotation of the object relative to the rotation on the road reference line */
    val referenceLinePointRelativeRotation get() = orientation.toRotation2D().toRotation3D() + Rotation3D.of(hOffset, pitch, roll)

    /** pose of the object relative to the pose on the road reference line */
    val referenceLinePointRelativePose
        get() = Pose3D(referenceLinePointRelativePosition, referenceLinePointRelativeRotation)

    // Methods
    fun containsRectangle() = width.isDefined() && height.isDefined()
    fun containsVerticalLine() = width.isEmpty() && height.isDefined()
    fun containsHorizontalLine() = width.isDefined() && height.isEmpty()

    companion object
}
