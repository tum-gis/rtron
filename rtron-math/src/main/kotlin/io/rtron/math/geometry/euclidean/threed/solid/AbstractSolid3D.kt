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

package io.rtron.math.geometry.euclidean.threed.solid

import arrow.core.NonEmptyList
import arrow.core.toNonEmptyListOrNull
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.Geometry3DVisitor
import io.rtron.math.geometry.euclidean.threed.surface.Polygon3D
import io.rtron.math.range.Tolerable

/**
 * Abstract class for all geometric solid objects in 3D.
 */
abstract class AbstractSolid3D : AbstractGeometry3D(), Tolerable {

    /**
     * Calculates the polygons for the respective solid geometry within the local coordinate system of the surface.
     */
    abstract fun calculatePolygonsLocalCS(): NonEmptyList<Polygon3D>

    /**
     * Calculates the polygons for the respective solid geometry and transforms it to the global coordinate system.
     */
    fun calculatePolygonsGlobalCS(): NonEmptyList<Polygon3D> =
        calculatePolygonsLocalCS().let { polygonList ->
            affineSequence.solve().transform(polygonList).let { it.toNonEmptyListOrNull()!! }
        }

    override fun accept(visitor: Geometry3DVisitor) = visitor.visit(this)
}
