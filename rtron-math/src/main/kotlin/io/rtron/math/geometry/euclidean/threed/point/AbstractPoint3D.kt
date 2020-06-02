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

package io.rtron.math.geometry.euclidean.threed.point

import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D


/**
 * Abstract class for all geometric point objects in 3D.
 */
abstract class AbstractPoint3D : AbstractGeometry3D() {

    /**
     * Returns the point in the local coordinate system.
     */
    abstract fun calculatePointLocalCS(): Vector3D

    /**
     * Returns the point in the global coordinate system.
     */
    fun calculatePointGlobalCS(): Vector3D = affineSequence.solve().transform(calculatePointLocalCS())
}
