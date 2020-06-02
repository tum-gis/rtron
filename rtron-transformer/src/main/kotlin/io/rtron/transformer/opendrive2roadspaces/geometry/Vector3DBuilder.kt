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

package io.rtron.transformer.opendrive2roadspaces.geometry

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.transform.Affine3D
import io.rtron.math.transform.AffineSequence3D
import io.rtron.model.opendrive.road.objects.RoadObjectsObject
import io.rtron.model.opendrive.road.signals.RoadSignalsSignal


/**
 * Builder for vectors in 3D from the OpenDRIVE data model.
 */
class Vector3DBuilder {

    // Methods

    /**
     * Builds a single point from an OpenDRIVE road object. The building of a point is suppressed, if a more detailed
     * geometry is available within the [RoadObjectsObject].
     *
     * @param srcRoadObject road object of OpenDRIVE
     * @param curveAffine affine transformation matrix at the reference curve
     * @param force true, if the point generation shall be forced
     */
    fun buildVector3Ds(srcRoadObject: RoadObjectsObject, curveAffine: Affine3D, force: Boolean = false):
            Result<Vector3D, IllegalArgumentException> =
            if (srcRoadObject.isPoint() || force) {
                val affineSequence = AffineSequence3D.of(curveAffine)
                val vector = srcRoadObject.referenceLinePointRelativePosition
                        .copy(affineSequence = affineSequence)
                Result.success(vector)
            } else Result.error(java.lang.IllegalArgumentException("Not a point geometry."))

    /**
     * Builds a single point from an OpenDRIVE road signal. The building of a point is suppressed, if a more detailed
     * geometry is available within the [RoadObjectsObject].
     *
     * @param srcRoadSignal road signal of OpenDRIVE
     * @param curveAffine affine transformation matrix at the reference curve
     * @param force true, if the point generation shall be forced
     */
    fun buildVector3Ds(srcRoadSignal: RoadSignalsSignal, curveAffine: Affine3D, force: Boolean = false):
            Result<Vector3D, IllegalArgumentException> =
            if (srcRoadSignal.isPoint() || force) {
                val affineSequence = AffineSequence3D.of(curveAffine)
                val vector = srcRoadSignal.referenceLinePointRelativePosition
                        .copy(affineSequence = affineSequence)
                Result.success(vector)
            } else Result.error(java.lang.IllegalArgumentException("Not a point geometry."))
}
