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

package io.rtron.math.container

import com.github.kittinunf.result.Result
import io.rtron.math.range.DefinableDomain
import io.rtron.math.range.Range
import io.rtron.math.range.RangeSet
import io.rtron.math.range.containsConsecutivelyIntersectingRanges
import io.rtron.math.range.fuzzyContains
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.range.shift
import io.rtron.std.handleSuccess
import io.rtron.std.hasSameSizeAs
import io.rtron.std.isSortedBy

/**
 * Concatenates a list of [members] with a locally defined domain to a container with an absolutely defined domain.
 * Requests to the container can be performed in the absolute domain, and the container will translate them to the
 * local domain.
 *
 * @param members members that are locally defined
 * @param absoluteDomains absolute domains of the respective member that is defined locally
 * @param absoluteStarts absolute starts of the respective member that is defined locally
 */
class ConcatenationContainer<T : DefinableDomain<Double>>(
    private val members: List<T>,
    private val absoluteDomains: List<Range<Double>>,
    private val absoluteStarts: List<Double>,
    private val tolerance: Double = 0.0
) {

    // Properties and Initializers
    init {
        require(members.isNotEmpty()) { "Must contain members for concatenation." }
        require(absoluteDomains.hasSameSizeAs(members)) { "Equally sized absoluteDomains and members required." }
        require(absoluteStarts.hasSameSizeAs(members)) { "Equally sized absoluteStart and members required." }

        // requirement: lower and upper domain boundaries (apart from the first and last entry)
        val absoluteDomainsWithoutStart =
            if (absoluteDomains.first().hasLowerBound()) absoluteDomains
            else absoluteDomains.drop(1)
        val absoluteDomainsWithoutEndings =
            if (absoluteDomainsWithoutStart.last().hasUpperBound()) absoluteDomainsWithoutStart
            else absoluteDomainsWithoutStart.dropLast(1)
        require(absoluteDomainsWithoutEndings.all { it.hasLowerBound() && it.hasUpperBound() }) { "All absolute domains (apart from the first and last one) must have an upper and lower bound." }
        require(absoluteDomainsWithoutEndings.isSortedBy { it.lowerEndpointOrNull()!! }) { "Provided absolute domains must be sorted." }

        // requirement: no intersecting domains
        require(!absoluteDomains.containsConsecutivelyIntersectingRanges()) { "Absolute domains must not contain intersecting ranges." }
        require(RangeSet(absoluteDomains.toSet()).numberOfDisconnectedRanges() == 1) { "Absolute domains must be connected." }

        require(
            members.zip(absoluteStarts)
                .map { it.first.domain.shift(it.second) }
                .zip(absoluteDomains)
                .all { it.first.fuzzyEncloses(it.second, tolerance) }
        ) { "The local domains must be defined everywhere where the absolute (shifted) domain is also defined." }
    }

    val domain = RangeSet(absoluteDomains.toSet()).span()

    // Methods

    /**
     * Small helper class storing the local parameter and the corresponding [member].
     *
     * @param localParameter parameter translated into a member local parameter
     * @param member corresponding local class
     */
    data class LocalRequest<T>(val localParameter: Double, val member: T)

    /**
     * Returns the selected member and the locally translated parameter.
     *
     * @param parameter absolute parameter
     */
    fun strictSelectMember(parameter: Double): Result<LocalRequest<T>, Exception> {
        val selection = absoluteDomains
            .withIndex()
            .filter { parameter in it.value }
            .map { it.index }
        return handleSelection(parameter, selection)
    }

    /**
     * Returns the selected member and the locally translated parameter. First applies a strict member selection and
     * then relaxes the member choice by a fuzzy selection.
     *
     * @param parameter absolute parameter
     * @param tolerance applied tolerance for the fuzzy selection
     */
    fun fuzzySelectMember(parameter: Double, tolerance: Double): Result<LocalRequest<T>, Exception> {
        strictSelectMember(parameter).handleSuccess { return it }

        val selection = absoluteDomains
            .withIndex()
            .filter { it.value.fuzzyContains(parameter, tolerance) }
            .map { it.index }
        return handleSelection(parameter, selection)
    }

    /**
     * Returns the selected member with the local parameter, in case of clear choice. If the choice is not clear,
     * errors with corresponding messages are returned.
     *
     * @param parameter absolute parameter
     * @param selection list of selected members
     */
    private fun handleSelection(parameter: Double, selection: List<Int>):
        Result<LocalRequest<T>, Exception> = when (selection.size) {

            0 -> Result.error(
                IllegalArgumentException("Parameter x=$parameter must be within in the domain $absoluteDomains.")
            )
            1 -> {
                val localParameter = parameter - absoluteStarts[selection.first()]
                Result.success(LocalRequest(localParameter, members[selection.first()]))
            }
            else -> Result.error(IllegalStateException("Parameter x=$parameter yields multiple members."))
        }
}
