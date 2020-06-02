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
import io.rtron.math.range.*
import io.rtron.std.cumulativeSum
import io.rtron.std.handleSuccess


/**
 * Concatenates a list of members according to their domain. The domain of each member must start at zero and
 * be finite, except the last member.
 *
 * @param members members to be concatenated
 */
class ConcatenationContainer<T : DefinableDomain<Double>>(
        members: List<T>
) {

    // Properties and Initializers
    init {
        require(members.isNotEmpty())
        { "Must contain members for concatenation." }
        require(members.all { it.domain.hasLowerBound() && it.domain.lowerEndpointOrNull() == 0.0 })
        { "Domain of all members must start at zero." }
        require(members.all { it.domain.length > 0.0 })
        { "Domain of all members must greater than zero." }
        require(members.dropLast(1).all { it.domain.hasUpperBound() && it.domain.upperEndpointOrNull()!!.isFinite() })
        { "All members (except the last one) must have a domain with an upper and finite bound." }
    }

    /** domain lengths of each member */
    private val domainLengths: List<Double> = members.map { it.domain.length }
    /** absolute starts of each member after concatenating them */
    private val absoluteStarts: List<Double> = domainLengths.dropLast(1).cumulativeSum()
    /** domain of each member in absolute values */
    private val absoluteDomains: List<Range<Double>>
    /** domain of this container */
    val domain: Range<Double>

    init {
        absoluteDomains = absoluteStarts.zip(domainLengths)
                .dropLast(1).map { Range.closedOpen(it.first, it.first + it.second) } +
                Range.closed(absoluteStarts.last(), absoluteStarts.last() + domainLengths.last())

        val absoluteRangeSet = absoluteDomains.toSet().unionRanges()
        domain = absoluteRangeSet.asRanges().first()

        assert(absoluteRangeSet.numberOfDisconnectedRanges() == 1)
        { "Must have exactly one connected range for the container." }
    }

    /**
     * Small helper class storing the absolute domain and the actual member together.
     *
     * @param absoluteDomain absolute domain of the [member]
     * @param member member which has the [absoluteDomains]
     */
    private data class AbsoluteDomainMember<T>(val absoluteDomain: Range<Double>, val member: T)

    /** list of absolute domains and their members */
    private val absoluteDomainMembers: List<AbsoluteDomainMember<T>> =
            absoluteDomains.zip(members).map { AbsoluteDomainMember(it.first, it.second) }


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
        val selection = absoluteDomainMembers
                .filter { parameter in it.absoluteDomain }
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

        val selection = absoluteDomainMembers
                .filter { it.absoluteDomain.fuzzyContains(parameter, tolerance) }
        return handleSelection(parameter, selection)
    }

    /**
     * Returns the selected member with the local parameter, in case of clear choice. If the choice is not clear,
     * errors with corresponding messages are returned.
     *
     * @param parameter absolute parameter
     * @param selection list of selected members
     */
    private fun handleSelection(parameter: Double, selection: List<AbsoluteDomainMember<T>>):
            Result<LocalRequest<T>, Exception> = when (selection.size) {

        0 -> Result.error(
                IllegalArgumentException("Parameter x=$parameter must be within in the domain $absoluteDomains."))
        1 -> {
            val localParameter = parameter - selection.first().absoluteDomain.lowerEndpointOrNull()!!
            Result.success(LocalRequest(localParameter, selection.first().member))
        }
        else -> Result.error(IllegalStateException("Parameter x=$parameter yields multiple members."))
    }

}
