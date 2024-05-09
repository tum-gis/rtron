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

package io.rtron.model.roadspaces

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.flattenOption
import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.right
import arrow.core.some
import arrow.core.toNonEmptyListOrNull
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.geometry.toIllegalStateException
import io.rtron.math.processing.isColinear
import io.rtron.math.processing.removeRedundantVerticesOnLineSegmentsEnclosing
import io.rtron.model.AbstractModel
import io.rtron.model.roadspaces.common.LongitudinalFillerSurface
import io.rtron.model.roadspaces.identifier.JunctionIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.LongitudinalLaneRangeIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.junction.Junction
import io.rtron.model.roadspaces.roadspace.ContactPoint
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.std.filterWithNextEnclosing
import io.rtron.std.getValueEither
import io.rtron.std.handleEmpty

/**
 * The [RoadspacesModel] is a parametric implementation of the objects within a road space and is capable of generating
 * surface based representations. Therefore, it can serve as intermediate model, as it can read the parametric modeling
 * approach of OpenDRIVE and generate the surface based modeling approach of CityGML.
 */
class RoadspacesModel(
    val header: Header,
    roadspaces: List<Roadspace>,
    junctions: List<Junction>,
) : AbstractModel() {
    // Properties and Initializers
    init {
        require(
            roadspaces.distinctBy { it.id }.size == roadspaces.size,
        ) { "Each roadspace identifier must not be assigned more than once." }
        require(junctions.distinctBy { it.id }.size == junctions.size) { "Each junction identifier must not be assigned more than once." }
    }

    private val roadspaces: Map<RoadspaceIdentifier, Roadspace> = roadspaces.associateBy { it.id }
    private val junctions: Map<JunctionIdentifier, Junction> = junctions.associateBy { it.id }

    /** Identifiers of all available roadspace. */
    val roadspaceIdentifiers get() = roadspaces.keys

    /** Identifiers of all available junctions. */
    val junctionIdentifiers get() = junctions.keys

    val numberOfRoadspaces get() = roadspaces.size
    val numberOfJunctions get() = junctions.size

    init {
        val linkedRoadspacesByRoads = roadspaces.flatMap { it.road.linkage.getAllUsedRoadspaceIds() }.distinct()
        require(
            roadspaceIdentifiers.containsAll(linkedRoadspacesByRoads),
        ) { "All roadspaces that are linked to from other roadspaces must exist." }

        val linkedJunctionsByRoads = roadspaces.flatMap { it.road.linkage.getAllUsedJunctionIds() }.distinct()
        require(
            junctionIdentifiers.containsAll(linkedJunctionsByRoads),
        ) { "All junctions that are linked to from other roadspaces must exist." }
        require(linkedJunctionsByRoads.size == numberOfJunctions) { "All junctions must be referenced by at least one roadspace." }
    }

    // Methods

    /** Returns the [Roadspace] with specific [roadspaceIdentifier]. */
    fun getRoadspace(roadspaceIdentifier: RoadspaceIdentifier): Either<IllegalArgumentException, Roadspace> =
        roadspaces.getValueEither(
            roadspaceIdentifier,
        ).mapLeft { it.toIllegalArgumentException() }

    /** Returns the [Junction] with specific [junctionIdentifier]. */
    fun getJunction(junctionIdentifier: JunctionIdentifier): Either<IllegalArgumentException, Junction> =
        junctions.getValueEither(junctionIdentifier).mapLeft { it.toIllegalArgumentException() }

    /** Returns a sorted list of all raodspace names. */
    fun getAllRoadspaceNames(): List<String> = getAllRoadspaces().map { it.name }.distinct().sorted()

    /** Returns all available [Roadspace]s. */
    fun getAllRoadspaces(): Collection<Roadspace> = roadspaces.values

    /** Returns all available [Junction]s. */
    fun getAllJunctions(): Collection<Junction> = junctions.values

    /** Returns a list of all available [Lane]s (without center lanes). */
    fun getAllLeftRightLanes(): List<Lane> = roadspaces.values.flatMap { it.road.getAllLeftRightLanes() }

    /** Returns a list of all [RoadspaceIdentifier] of roadspaces which are not located in a [Junction] and have
     *  the [roadspaceName]. */
    fun getAllRoadspaceIdentifiersNotLocatedInJunctions(roadspaceName: String): List<RoadspaceIdentifier> =
        getAllRoadspaces()
            .filter { it.name == roadspaceName }
            .filter { !it.road.isLocatedInJunction() }
            .map { it.id }

    /** Returns a list of all [Roadspace]s that are not located in a junction. */
    fun getAllRoadspacesNotLocatedInJunction() = getAllRoadspaces().filter { !it.road.isLocatedInJunction() }

    /** Returns a list of all [Roadspace]s that are located in a junction. */
    fun getAllRoadspacesLocatedInJunction() = getAllRoadspaces().filter { it.road.isLocatedInJunction() }

    /** Returns a list of [Junction]s which contain at least one [Roadspace] with the name [roadspaceName]. */
    fun getAllJunctionIdentifiersContainingRoadspaces(roadspaceName: String): List<JunctionIdentifier> =
        getAllRoadspacesLocatedInJunction()
            .filter { it.name == roadspaceName }
            .map { it.road.linkage.belongsToJunctionId }
            .flattenOption()
            .distinct()

    /** Returns a list of [Roadspace]s that belong to the junction with [junctionIdentifier]. */
    fun getRoadspacesWithinJunction(junctionIdentifier: JunctionIdentifier): Either<Exception, List<Roadspace>> =
        either {
            val junction = getJunction(junctionIdentifier).bind()
            val connectingRoadspaces = junction.getConnectingRoadspaceIds().map { getRoadspace(it).bind() }
            connectingRoadspaces
        }

    /**
     * Returns an identifier list of lanes that precede this lane.
     *
     * @param laneId lane identifier for which the predecessor lanes shall be found
     */
    fun getPredecessorLaneIdentifiers(laneId: LaneIdentifier): Either<IllegalArgumentException, List<LaneIdentifier>> {
        val road =
            roadspaces.getValueEither(laneId.toRoadspaceIdentifier())
                .getOrElse { throw it.toIllegalArgumentException() }.road

        return when {
            road.isInFirstLaneSection(laneId) && road.linkage.predecessorRoadspaceContactPointId.isSome() ->
                getPredecessorLanesBetweenRoads(laneId)
            road.isInFirstLaneSection(laneId) && road.linkage.predecessorJunctionId.isSome() ->
                getPredecessorLanesBetweenRoadsInJunction(laneId)
            !road.isInFirstLaneSection(laneId) -> getPredecessorLanesWithinRoad(laneId)
            else -> emptyList<LaneIdentifier>().right()
        }
    }

    /**
     * Returns an identifier list of lanes that follow this lane.
     *
     * @param laneId lane identifier for which the successor lanes shall be found
     */
    fun getSuccessorLaneIdentifiers(laneId: LaneIdentifier): Either<IllegalArgumentException, List<LaneIdentifier>> {
        val road =
            roadspaces.getValueEither(laneId.toRoadspaceIdentifier())
                .getOrElse { throw it.toIllegalArgumentException() }.road

        return when {
            road.isInLastLaneSection(laneId) && road.linkage.successorRoadspaceContactPointId.isSome() ->
                getSuccessorLanesBetweenRoads(laneId)
            road.isInLastLaneSection(laneId) && road.linkage.successorJunctionId.isSome() ->
                getSuccessorLanesBetweenRoadsInJunction(laneId)
            !road.isInLastLaneSection(laneId) -> getSuccessorLanesWithinRoad(laneId)
            else -> emptyList<LaneIdentifier>().right()
        }
    }

    fun getLongitudinalFillerSurfaces(laneId: LaneIdentifier): Either<Exception, List<LongitudinalFillerSurface>> =
        either {
            val successorLaneIds = getSuccessorLaneIdentifiers(laneId).bind()

            val fillerSurfaces = successorLaneIds.map { getLongitudinalFillerSurface(laneId, it) }.flattenOption()
            fillerSurfaces
        }

    /**
     * Returns the filler surface which is located between the lane of [laneId] and [successorLaneId].
     *
     * @param laneId identifier of the first lane
     * @param successorLaneId identifier of the second lane
     */
    private fun getLongitudinalFillerSurface(
        laneId: LaneIdentifier,
        successorLaneId: LaneIdentifier,
    ): Option<LongitudinalFillerSurface> {
        val road =
            roadspaces
                .getValueEither(laneId.laneSectionIdentifier.roadspaceIdentifier)
                .getOrElse { throw it.toIllegalArgumentException() }.road
        val successorRoad =
            roadspaces
                .getValueEither(successorLaneId.laneSectionIdentifier.roadspaceIdentifier)
                .getOrElse { throw it.toIllegalArgumentException() }.road
        val surface = buildLongitudinalFillerSurfaceGeometry(laneId, successorLaneId, road, successorRoad).handleEmpty { return None }
        val longitudinalFillerSurfaceId = LongitudinalLaneRangeIdentifier(laneId, successorLaneId)

        val fillerSurface =
            when {
                laneId.isWithinSameRoad(successorLaneId) -> LongitudinalFillerSurface.ofWithinRoad(longitudinalFillerSurfaceId, surface)
                else -> LongitudinalFillerSurface.ofBetweenRoad(longitudinalFillerSurfaceId, surface)
            }
        return Some(fillerSurface)
    }

    /**
     * Returns the geometry of a longitudinal filler surface between the current lane and the successor lane.
     *
     * @param laneId identifier of the lane
     * @param successorLaneId id of the successor lane
     * @param road road to which [laneId] belongs
     * @param successorRoad road to which the successor lane belongs
     */
    fun buildLongitudinalFillerSurfaceGeometry(
        laneId: LaneIdentifier,
        successorLaneId: LaneIdentifier,
        road: Road,
        successorRoad: Road,
    ): Option<AbstractSurface3D> {
        val currentVertices =
            listOf(road.getLeftLaneBoundary(laneId), road.getRightLaneBoundary(laneId))
                .map { currentCurve -> currentCurve.getOrElse { throw it } }
                .map {
                        currentCurve ->
                    currentCurve.calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrElse { throw it }
                }

        // false, if the successor lane is connected by its end (leads to swapping of the vertices)
        val successorContactStart =
            if (laneId.isWithinSameRoad(successorLaneId)) {
                true
            } else {
                !road.linkage.successorRoadspaceContactPointId.map { it.roadspaceContactPoint }
                    .isSome { it == ContactPoint.END }
            }

        val successorLaneBoundaries =
            listOf(successorRoad.getRightLaneBoundary(successorLaneId), successorRoad.getLeftLaneBoundary(successorLaneId))
                .map { it.getOrElse { return None } }

        val successorVertices =
            when (successorContactStart) {
                true ->
                    successorLaneBoundaries
                        .map {
                            it.calculateStartPointGlobalCS()
                                .mapLeft { it.toIllegalStateException() }
                                .getOrElse { throw it }
                        }
                false ->
                    successorLaneBoundaries.map {
                        it.calculateEndPointGlobalCS().mapLeft { it.toIllegalStateException() }.getOrElse { throw it }
                    }.reversed()
            }

        val tolerance = minOf(road.surface.tolerance, successorRoad.surface.tolerance)
        val fillerSurfaceVertices =
            (currentVertices + successorVertices)
                .filterWithNextEnclosing { a, b -> a.fuzzyUnequals(b, tolerance) }
                .removeRedundantVerticesOnLineSegmentsEnclosing(tolerance)

        return if (fillerSurfaceVertices.size < 3 || fillerSurfaceVertices.isColinear(tolerance)) {
            None
        } else {
            LinearRing3D(fillerSurfaceVertices.toNonEmptyListOrNull()!!, tolerance).some()
        }
    }

    /**
     * Returns a list of [LaneIdentifier]s that precede the lane with [laneId] and are located within the same road.
     *
     * @param laneId lane identifier, which must not be located within the last section of the road
     */
    private fun getPredecessorLanesWithinRoad(laneId: LaneIdentifier): Either<IllegalArgumentException, List<LaneIdentifier>> {
        val road =
            roadspaces.getValueEither(laneId.toRoadspaceIdentifier())
                .getOrElse { throw it.toIllegalArgumentException() }.road
        check(!road.isInFirstLaneSection(laneId)) { "Current lane must not be located in the first lane section of the road." }

        return road.getLane(laneId)
            .getOrElse { throw it }
            .predecessors
            .map { LaneIdentifier(it, laneId.laneSectionIdentifier.getPreviousLaneSectionIdentifier()) }
            .let { Either.Right(it) }
    }

    /**
     * Returns a list of [LaneIdentifier]s that succeed the lane with [laneId] and are located within the same road.
     *
     * @param laneId lane identifier, which must not be located within the last section of the road
     */
    private fun getSuccessorLanesWithinRoad(laneId: LaneIdentifier): Either<IllegalArgumentException, List<LaneIdentifier>> {
        val road =
            roadspaces.getValueEither(laneId.toRoadspaceIdentifier())
                .getOrElse { throw it.toIllegalArgumentException() }.road
        check(!road.isInLastLaneSection(laneId)) { "Current lane must not be located in the last lane section of the road." }

        return road.getLane(laneId)
            .getOrElse { throw it }
            .successors
            .map { LaneIdentifier(it, laneId.laneSectionIdentifier.getNextLaneSectionIdentifier()) }
            .let { Either.Right(it) }
    }

    /**
     * Returns a list of [LaneIdentifier]s that precede the lane with [laneId] and are connected via another road.
     *
     * @param laneId lane identifier, which must be located at the first lane section on a road which follows another
     * road
     */
    private fun getPredecessorLanesBetweenRoads(laneId: LaneIdentifier): Either<IllegalArgumentException, List<LaneIdentifier>> {
        val road =
            roadspaces.getValueEither(laneId.toRoadspaceIdentifier())
                .getOrElse { throw it.toIllegalArgumentException() }.road
        val roadPredecessorRoadspaceContactPoint =
            road.linkage
                .predecessorRoadspaceContactPointId
                .handleEmpty { throw IllegalArgumentException("Current road must have a predecessor road.") }

        val predecessorRoad =
            roadspaces.getValueEither(roadPredecessorRoadspaceContactPoint.roadspaceIdentifier)
                .getOrElse { return Either.Right(emptyList()) }.road
        val predecessorLaneSectionIdentifier = predecessorRoad.getLaneSectionIdentifier(roadPredecessorRoadspaceContactPoint)

        return road.getLane(laneId)
            .getOrElse { throw it }
            .successors
            .map { LaneIdentifier(it, predecessorLaneSectionIdentifier) }
            .let { Either.Right(it) }
    }

    /**
     * Returns a list of [LaneIdentifier]s that succeed the lane with [laneId] and are connected via another road.
     *
     * @param laneId lane identifier, which must be located at the last lane section on a road which leads into
     * another road
     */
    private fun getSuccessorLanesBetweenRoads(laneId: LaneIdentifier): Either<IllegalArgumentException, List<LaneIdentifier>> {
        val road =
            roadspaces.getValueEither(laneId.toRoadspaceIdentifier())
                .getOrElse { throw it.toIllegalArgumentException() }.road
        val roadSuccessorRoadspaceContactPoint =
            road.linkage
                .successorRoadspaceContactPointId
                .handleEmpty { throw IllegalArgumentException("Current road must have a successor road.") }

        val successorRoad =
            roadspaces.getValueEither(roadSuccessorRoadspaceContactPoint.roadspaceIdentifier)
                .getOrElse { return Either.Right(emptyList()) }.road
        val successorLaneSectionIdentifier = successorRoad.getLaneSectionIdentifier(roadSuccessorRoadspaceContactPoint)

        return road.getLane(laneId)
            .getOrElse { throw it }
            .successors
            .map { LaneIdentifier(it, successorLaneSectionIdentifier) }
            .let { Either.Right(it) }
    }

    /**
     * Returns a list of [LaneIdentifier]s that precede the lane with [laneId] and are connected via a junction.
     *
     * @param laneId lane identifier, which must be located at the first lane section on a road which is followed into
     * a junction
     */
    private fun getPredecessorLanesBetweenRoadsInJunction(laneId: LaneIdentifier): Either<IllegalArgumentException, List<LaneIdentifier>> {
        val road =
            roadspaces.getValueEither(laneId.toRoadspaceIdentifier())
                .getOrElse { throw it.toIllegalArgumentException() }.road
        check(road.isInFirstLaneSection(laneId)) { "Current lane must be located in the last lane section of the road." }
        val predecessorJunctionId =
            road.linkage.predecessorJunctionId.handleEmpty {
                throw IllegalStateException("Current road must have a predecessor junction.")
            }

        val predecessorJunction =
            junctions
                .getValueEither(predecessorJunctionId)
                .getOrElse { return Either.Right(emptyList()) }

        return predecessorJunction
            .getSuccessorLane(laneId)
            .let { Either.Right(it) }
    }

    /**
     * Returns a list of [LaneIdentifier]s that succeed the lane with [laneId] and are connected via a junction.
     *
     * @param laneId lane identifier, which must be located at the last lane section on a road which leads into
     * a junction
     */
    private fun getSuccessorLanesBetweenRoadsInJunction(laneId: LaneIdentifier): Either<IllegalArgumentException, List<LaneIdentifier>> {
        val road =
            roadspaces.getValueEither(laneId.toRoadspaceIdentifier())
                .getOrElse { throw it.toIllegalArgumentException() }.road
        check(road.isInLastLaneSection(laneId)) { "Current lane must be located in the last lane section of the road." }
        val successorJunctionId =
            road.linkage.successorJunctionId.handleEmpty {
                throw IllegalStateException("Current road must have a successor junction.")
            }

        val successorJunction =
            junctions
                .getValueEither(successorJunctionId)
                .getOrElse { return Either.Right(emptyList()) }

        return successorJunction
            .getSuccessorLane(laneId)
            .let { Either.Right(it) }
    }
}
