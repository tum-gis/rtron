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

package io.rtron.model.roadspaces.roadspace.road

import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList


/**
 * Represents a lane within a lane section.
 *
 * @param id identifier of the lane
 * @param width width of the lane as parametric function
 * @param innerHeightOffset extra vertical offset height on the inner lane boundary
 * @param outerHeightOffset extra vertical offset height on the outer lane boundary
 * @param level if true, the lane is kept on level; if false, superelevation is applied to the lane
 * @param predecessors list of predecessor lane ids
 * @param successors list of successor lane ids
 * @param attributes information attributes to the lane
 */
data class Lane(
        val id: LaneIdentifier,
        val width: UnivariateFunction,
        val innerHeightOffset: UnivariateFunction,
        val outerHeightOffset: UnivariateFunction,
        val level: Boolean,
        val predecessors: List<Int>,
        val successors: List<Int>,
        val attributes: AttributeList
)
