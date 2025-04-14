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

package io.rtron.transformer.converter.opendrive2roadspaces.roadspaces

import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Some
import arrow.core.flattenOption
import arrow.core.getOrElse
import arrow.core.some
import arrow.core.toNonEmptyListOrNone
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.math.range.Range
import io.rtron.math.range.length
import io.rtron.math.std.fuzzyEquals
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.lane.ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange
import io.rtron.model.opendrive.lane.ERoadMarkType
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMark
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMarkExplicitLine
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLCRLaneRoadMarkTypeLine
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.model.roadspaces.roadspace.attribute.attributes
import io.rtron.model.roadspaces.roadspace.road.LaneChange
import io.rtron.model.roadspaces.roadspace.road.RoadMarking
import io.rtron.transformer.converter.opendrive2roadspaces.Opendrive2RoadspacesParameters
import io.rtron.transformer.issues.opendrive.of
import kotlin.math.floor

enum class RoadMarkRepresentationType {
    GENERAL,
    REGULAR,
    EXPLICIT,
    ;

    companion object {
        fun fromRoadMark(roadMark: RoadLanesLaneSectionLCRLaneRoadMark): RoadMarkRepresentationType {
            if (roadMark.containsExplicitLines()) return EXPLICIT
            if (roadMark.containsTypeLines()) return REGULAR
            return GENERAL
        }

        fun getMostDetailedRepresentation(
            roadMarkRepresentationTypes: NonEmptyList<RoadMarkRepresentationType>,
        ): RoadMarkRepresentationType {
            if (roadMarkRepresentationTypes.any { it == EXPLICIT }) return EXPLICIT
            if (roadMarkRepresentationTypes.any { it == REGULAR }) return REGULAR
            return GENERAL
        }
    }
}

/**
 * Registry for the highest representation type for each road mark type.
 */
class RoadMarkRepresentationRegistry(
    val mode: Map<ERoadMarkType, RoadMarkRepresentationType>,
) {
    // Methods

    fun getRoadMarkTypeRepresentation(roadMarkType: ERoadMarkType): RoadMarkRepresentationType =
        mode.getOrElse(roadMarkType) { throw IllegalStateException("Unknown road mark type $roadMarkType.") }

    companion object {
        fun fromHighestOccurrence(opendriveModel: OpendriveModel): RoadMarkRepresentationRegistry {
            val laneSections =
                opendriveModel.road
                    .flatMap { it.lanes.laneSection }
            val roadMarks =
                laneSections
                    .flatMap { it.center.lane.roadMark } +
                    laneSections
                        .map { it.left }.flattenOption()
                        .flatMap { it.lane }.flatMap { it.roadMark } +
                    laneSections
                        .map { it.left }.flattenOption()
                        .flatMap { it.lane }.flatMap { it.roadMark }

            val highestRepresentationPerMarkingType =
                roadMarks
                    .map { it.typeAttribute to RoadMarkRepresentationType.fromRoadMark(it) }
                    .groupBy { it.first }
                    .map { (key, values) ->
                        val valueModes =
                            values.map { it.second }
                                .toNonEmptyListOrNone()
                                .getOrElse { throw IllegalStateException("No value mode found for road mark type $key.") }
                        val highestValueMode = RoadMarkRepresentationType.getMostDetailedRepresentation(valueModes)
                        key to highestValueMode
                    }.toMap()

            return RoadMarkRepresentationRegistry(highestRepresentationPerMarkingType)
        }
    }
}

/**
 * Builder for [RoadMarking] objects of the RoadSpaces data model.
 */
class RoadMarkingBuilder(
    private val parameters: Opendrive2RoadspacesParameters,
) {
    // Methods

    /**
     * Builds a list of road markings ([roadMark]).
     *
     * @param curvePositionDomain curve position domain (relative to the lane section) where the road markings is defined
     * @param roadMark road marking entries of the OpenDRIVE data model
     */
    fun buildRoadMarkings(
        curvePositionDomain: Range<Double>,
        roadMark: NonEmptyList<RoadLanesLaneSectionLCRLaneRoadMark>,
        roadMarkRepresentationRegistry: RoadMarkRepresentationRegistry,
    ): ContextIssueList<List<RoadMarking>> {
        require(curvePositionDomain.hasUpperBound()) { "curvePositionDomain must have an upper bound." }
        val roadMarkId =
            roadMark.head.additionalId.toEither {
                IllegalStateException("Additional outline ID must be available.")
            }.getOrElse { throw it }
        val issueList = DefaultIssueList()

        val curvePositionDomainEnd = curvePositionDomain.upperEndpointOrNull()!!
        val adjustedSrcRoadMark =
            roadMark
                .filter { it.sOffset in curvePositionDomain }
                .filter { !fuzzyEquals(it.sOffset, curvePositionDomainEnd, parameters.numberTolerance) }
        if (adjustedSrcRoadMark.size < roadMark.size) {
            issueList +=
                DefaultIssue.of(
                    "RoadMarkEntriesNotLocatedWithinSRange",
                    "Road mark entries have been removed, as the sOffset is not located within " +
                        "the local curve position domain ($curvePositionDomain) of the lane section.",
                    roadMarkId, Severity.WARNING, wasFixed = true,
                )
        }

        if (adjustedSrcRoadMark.isEmpty()) return ContextIssueList(emptyList(), issueList)

        val roadMarkingsWithIndex = adjustedSrcRoadMark.withIndex()
        val roadMarkings =
            roadMarkingsWithIndex.zipWithNext()
                .flatMap {
                    buildRoadMarking(it.first.value, it.first.index, it.second.value.sOffset, roadMarkRepresentationRegistry)
                } +
                buildRoadMarking(
                    roadMarkingsWithIndex.last().value,
                    roadMarkingsWithIndex.last().index,
                    curvePositionDomainEnd, roadMarkRepresentationRegistry,
                )

        return ContextIssueList(roadMarkings, issueList)
    }

    /**
     * Builds an individual road marking [roadMark].
     *
     * @param roadMark road mark entry of the OpenDRIVE data model
     * @param domainEndpoint upper domain endpoint for the domain of the road mark
     */
    private fun buildRoadMarking(
        roadMark: RoadLanesLaneSectionLCRLaneRoadMark,
        roadMarkIndex: Int,
        domainEndpoint: Double,
        roadMarkRepresentationRegistry: RoadMarkRepresentationRegistry,
    ): List<RoadMarking> {
        val domain = Range.closed(roadMark.sOffset, domainEndpoint)
        require(domain.length > parameters.numberTolerance) { "Length of road marking must be above zero and the tolerance threshold." }

        val typeRepresentation = roadMarkRepresentationRegistry.getRoadMarkTypeRepresentation(roadMark.typeAttribute)
        val generalAttributes =
            attributes("${parameters.attributesPrefix}roadMarking_") {
                attribute("index", roadMarkIndex)
                attribute("curvePositionStart", roadMark.sOffset)
                attribute("type", roadMark.typeAttribute.toString())
                attribute("weight", roadMark.weight.map { it.toString() })
                attribute("laneChange", roadMark.laneChange.map { it.toString() })
                attribute("color", roadMark.color.toString())
                attribute("material", roadMark.material)
                attribute("width", roadMark.width)
            }
        val laneChange =
            when (roadMark.laneChange.getOrElse { ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.BOTH }) {
                ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.INCREASE -> LaneChange.INCREASE
                ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.DECREASE -> LaneChange.DECREASE
                ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.BOTH -> LaneChange.BOTH
                ERoadLanesLaneSectionLCRLaneRoadMarkLaneChange.NONE -> LaneChange.NONE
            }

        return when (typeRepresentation) {
            RoadMarkRepresentationType.GENERAL -> listOf(RoadMarking(domain, roadMark.width, None, laneChange, generalAttributes))
            RoadMarkRepresentationType.REGULAR -> {
                roadMark.getTypeLines().fold(
                    ifEmpty = {
                        listOf()
                    },
                    ifSome = {
                        buildRoadMarkings(roadMark.sOffset, it, domain, laneChange, generalAttributes)
                    },
                )
            }
            RoadMarkRepresentationType.EXPLICIT -> {
                roadMark.getExplicitLines().fold(
                    ifEmpty = {
                        emptyList()
                    },
                    ifSome = {
                        buildRoadMarkings(roadMark.sOffset, it, laneChange, generalAttributes)
                    },
                )
            }
        }
    }

    private fun buildRoadMarkings(
        roadMarkingCurvePositionStart: Double,
        typeLines: NonEmptyList<RoadLanesLaneSectionLCRLaneRoadMarkTypeLine>,
        domain: Range<Double>,
        laneChange: LaneChange,
        generalAttributeList: AttributeList,
    ): List<RoadMarking> {
        require(domain.hasUpperBound()) { "Domain must have an upper bound." }

        return typeLines.withIndex()
            .flatMap { (currentIndex, currentTypeLine) ->
                val typeLineAttributes =
                    attributes("${parameters.attributesPrefix}roadMarking_typeLine_") {
                        attribute("index", currentIndex)
                        attribute("width", currentTypeLine.width)
                    }
                val currentTypeLineCurvePositionStart = roadMarkingCurvePositionStart + currentTypeLine.sOffset
                val lateralOffset =
                    if (currentTypeLine.tOffset < parameters.numberTolerance) None else Some(currentTypeLine.tOffset)

                val step = currentTypeLine.length + currentTypeLine.space
                (0..floor((domain.upperEndpointOrNull()!! - currentTypeLineCurvePositionStart) / step).toInt())
                    .map { currentRegularIndex ->
                        val start = currentTypeLineCurvePositionStart + currentRegularIndex * step
                        val end =
                            if (start + currentTypeLine.length < domain.upperEndpointOrNull()!!) {
                                start + currentTypeLine.length
                            } else {
                                domain.upperEndpointOrNull()!!
                            }
                        if (end - start < parameters.numberTolerance) return@map None

                        val attributes =
                            attributes("${parameters.attributesPrefix}roadMarking_typeLine_regular_") {
                                attribute("index", currentIndex)
                                attribute("curvePositionStart", start)
                                attribute("curvePositionEnd", end)
                            }

                        RoadMarking(
                            Range.closed(start, end),
                            currentTypeLine.width,
                            lateralOffset,
                            laneChange,
                            generalAttributeList + typeLineAttributes + attributes,
                        ).some()
                    }.flattenOption()
            }
    }

    private fun buildRoadMarkings(
        roadMarkingCurvePositionStart: Double,
        explicitLines: NonEmptyList<RoadLanesLaneSectionLCRLaneRoadMarkExplicitLine>,
        laneChange: LaneChange,
        generalAttributeList: AttributeList,
    ): NonEmptyList<RoadMarking> =
        explicitLines.withIndex().map { (currentIndex, currentExplicitLine) ->
            val attributes =
                attributes("${parameters.attributesPrefix}roadMarking_explicitLine_") {
                    attribute("index", currentIndex)
                    attribute("curvePositionStart", currentExplicitLine.sOffset)
                    attribute("curvePositionEnd", currentExplicitLine.sOffset + currentExplicitLine.length)
                    attribute("lateralOffset", currentExplicitLine.tOffset)
                    attribute("width", currentExplicitLine.width)
                }
            val lateralOffset = if (currentExplicitLine.tOffset < parameters.numberTolerance) None else Some(currentExplicitLine.tOffset)

            val currentEntryDomain =
                Range.closed(
                    roadMarkingCurvePositionStart + currentExplicitLine.sOffset,
                    roadMarkingCurvePositionStart + currentExplicitLine.sOffset + currentExplicitLine.length,
                )
            RoadMarking(currentEntryDomain, currentExplicitLine.width, lateralOffset, laneChange, generalAttributeList + attributes)
        }.toNonEmptyListOrNone().getOrElse { throw IllegalArgumentException("Explicit road markings must contain at least one entry.") }
}
