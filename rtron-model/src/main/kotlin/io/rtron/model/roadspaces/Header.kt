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

package io.rtron.model.roadspaces

import com.github.kittinunf.result.Result
import io.rtron.math.projection.CoordinateReferenceSystem


/**
 * Header of the [RoadspacesModel] containing the model's meta information.
 */
data class Header (
        val coordinateReferenceSystem: Result<CoordinateReferenceSystem, Exception>,

        val north: Double = Double.NaN,
        val south: Double = Double.NaN,
        val east: Double = Double.NaN,
        val west: Double = Double.NaN
)
