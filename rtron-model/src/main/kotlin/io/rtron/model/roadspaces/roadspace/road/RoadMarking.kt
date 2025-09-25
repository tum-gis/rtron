/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

import arrow.core.None
import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.some
import io.rtron.math.analysis.function.univariate.pure.ConstantFunction
import io.rtron.math.range.Range
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList

/**
 * Represents a single road marking.
 *
 * @param width lateral width of the road marking
 * @param attributes further information attributes
 */
data class RoadMarking(
    val domain: Range<Double>,
    val width: Option<Double>,
    val lateralOffset: Option<Double>,
    val laneChange: LaneChange,
    val attributes: AttributeList,
) {
    // Properties and Initializers
    init {
        require(domain.isNotEmpty()) { "The domain of the road marking's width must not be empty." }
        require(width.fold({ true }, { it > 0.0 })) { "The width of the road marking must be greater than zero." }
    }

    val widthFunction get(): Option<ConstantFunction> = width.map { ConstantFunction(it, domain) }

    val lateralOffsetFunctionOptional get(): Option<ConstantFunction> = lateralOffset.map { ConstantFunction(it, domain) }

    val lateralOffsetFunction get(): ConstantFunction = ConstantFunction(lateralOffset.getOrElse { 0.0 }, domain)

    // Methods

    fun getLeftOffsetFunction(): Option<ConstantFunction> {
        val offsetValue = width.map { 0.5 * it }.getOrElse { return None } + lateralOffset.getOrElse { 0.0 }
        return ConstantFunction(offsetValue, domain).some()
    }

    fun getRightOffsetFunction(): Option<ConstantFunction> {
        val offsetValue = width.map { -0.5 * it }.getOrElse { return None } + lateralOffset.getOrElse { 0.0 }
        return ConstantFunction(offsetValue, domain).some()
    }
}
