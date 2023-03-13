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

package io.rtron.math.geometry.euclidean.threed

import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.point.Vector3D
import io.rtron.math.geometry.euclidean.threed.solid.AbstractSolid3D
import io.rtron.math.geometry.euclidean.threed.solid.Cylinder3D
import io.rtron.math.geometry.euclidean.threed.solid.ParametricSweep3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.Circle3D
import io.rtron.math.geometry.euclidean.threed.surface.ParametricBoundedSurface3D

/**
 * Visitor interface for 3D geometries. Visitor pattern is applied to separate geometry transformation algorithms
 * from the actual object structure of the 3D geometry.
 */
interface Geometry3DVisitor {

    // point
    fun visit(vector3D: Vector3D)

    // curve
    fun visit(abstractCurve3D: AbstractCurve3D)

    // surface
    fun visit(abstractSurface3D: AbstractSurface3D)
    fun visit(circle3D: Circle3D)
    fun visit(parametricBoundedSurface3D: ParametricBoundedSurface3D)

    // solid
    fun visit(abstractSolid3D: AbstractSolid3D)
    fun visit(cylinder3D: Cylinder3D)
    fun visit(parametricSweep3D: ParametricSweep3D)

    // abstract geometry
    fun visit(abstractGeometry3D: AbstractGeometry3D)
}
