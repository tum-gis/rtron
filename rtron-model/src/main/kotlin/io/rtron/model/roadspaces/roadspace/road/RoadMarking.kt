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

import io.rtron.math.analysis.function.univariate.pure.ConstantFunction
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList

/**
 * Represents a single road marking.
 *
 * @param width lateral width of the road marking
 * @param attributes further information attributes
 */
data class RoadMarking(
    val width: ConstantFunction,
    val attributes: AttributeList
)
