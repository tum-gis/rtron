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

package io.rtron.math.geometry.euclidean.twod.surface

import io.rtron.math.geometry.euclidean.twod.AbstractGeometry2D
import io.rtron.math.geometry.euclidean.twod.point.Vector2D
import io.rtron.math.range.Tolerable

/**
 * Abstract class for all geometric surface objects in 2D.
 */
abstract class AbstractSurface2D : AbstractGeometry2D(), Tolerable {

    /**
     * Returns true, if [point] is located within the surface.
     */
    abstract fun contains(point: Vector2D): Boolean
}
