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

package io.rtron.math.geometry.euclidean.threed

import io.rtron.math.geometry.euclidean.AbstractGeometry
import io.rtron.math.transform.AffineSequence3D

/**
 * Abstract class for all geometric objects in 3D.
 */
abstract class AbstractGeometry3D : AbstractGeometry() {

    /**
     * List of affine transformation matrices to move and rotate the geometric object.
     */
    open val affineSequence: AffineSequence3D = AffineSequence3D.EMPTY

    /**
     * Accepting function so that a geometry visitor can pass by.
     */
    abstract fun accept(visitor: Geometry3DVisitor)
}
