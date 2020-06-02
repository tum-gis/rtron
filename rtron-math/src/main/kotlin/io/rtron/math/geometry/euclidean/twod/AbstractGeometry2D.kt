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

package io.rtron.math.geometry.euclidean.twod

import io.rtron.math.geometry.euclidean.AbstractGeometry
import io.rtron.math.transform.AffineSequence2D


/**
 * Abstract class for all geometric objects in 2D.
 */
abstract class AbstractGeometry2D : AbstractGeometry() {

    /**
     * List of affine transformation matrices to move and rotate the geometric object.
     */
    open val affineSequence: AffineSequence2D = AffineSequence2D.EMPTY
}
