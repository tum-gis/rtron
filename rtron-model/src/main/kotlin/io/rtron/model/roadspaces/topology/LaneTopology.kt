package io.rtron.model.roadspaces.topology

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.processing.removeLinearlyRedundantVertices
import io.rtron.model.roadspaces.roadspace.Roadspace
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.road.ContactPoint
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.RelativeLaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.Road
import io.rtron.model.roadspaces.topology.junction.Junction
import io.rtron.model.roadspaces.topology.junction.JunctionIdentifier
import io.rtron.std.*


/**
 * Represents a filler surface with it's [surface] geometry and information from which [laneId] to which
 * [successorLaneId] it fills.
 */
data class IdentifiableFillerSurface(val laneId: LaneIdentifier, val successorLaneId: LaneIdentifier,
                                     val surface: AbstractSurface3D)

/**
 * The lane topology provides the functionality to find predecessor and successor lanes.
 * Filler surfaces can be generated that are located between two lanes.
 *
 * @param roadspaces roadspaces which contain the road and lane linkage information
 * @param junctions junctions which contain lane linkage information
 */
class LaneTopology(
        private val roadspaces: Map<RoadspaceIdentifier, Roadspace>,
        private val junctions: Map<JunctionIdentifier, Junction>
) {

    // Methods

    /**
     * Returns an identifier list of lanes that follow this lane.
     *
     * @param laneId lane identifier for which the successor lane shall be found
     */
    fun getSuccessorLaneIdentifiers(laneId: LaneIdentifier): Result<List<LaneIdentifier>, IllegalArgumentException> {

        val road = roadspaces.getValueResult(laneId.toRoadspaceIdentifier())
                .handleFailure { throw it.error }.road

        return when {
            road.isInLastLaneSection(laneId) && road.linkage.successorRoadspaceId.isPresent() ->
                getSuccessorLanesBetweenRoads(laneId)
            road.isInLastLaneSection(laneId) && road.linkage.successorJunctionId.isPresent() ->
                getSuccessorLanesBetweenRoadsInJunction(laneId)
            !road.isInLastLaneSection(laneId) -> getSuccessorLanesWithinRoad(laneId)
            else -> return Result.success(emptyList())
        }
    }

    /**
     * Returns a list of filler surfaces that are located between the lane with [laneId] and it's successive
     * lanes.
     *
     * @param laneId lane id of the lane for which the successive filler surfaces shall be generated
     */
    fun getLongitudinalFillerSurfaces(laneId: LaneIdentifier): List<IdentifiableFillerSurface> =
            getSuccessorLaneIdentifiers(laneId)
                    .handleFailure { throw it.error }
                    .map { Triple(laneId, it, getLongitudinalFillerSurface(laneId, it)) }
                    .filter { it.third.isPresent() }
                    .map { IdentifiableFillerSurface(it.first, it.second, it.third.getOrNull()!!) }

    /**
     * Returns the filler surface which is located between the lane of [laneId] and [successorLaneId].
     *
     * @param laneId identifier of the first lane
     * @param successorLaneId identifier of the second lane
     */
    private fun getLongitudinalFillerSurface(laneId: LaneIdentifier, successorLaneId: LaneIdentifier):
            Optional<AbstractSurface3D> {

        val road = roadspaces
                .getValueResult(laneId.laneSectionIdentifier.roadspaceIdentifier)
                .handleFailure { throw it.error }.road
        val successorRoad = roadspaces
                .getValueResult(successorLaneId.laneSectionIdentifier.roadspaceIdentifier)
                .handleFailure { throw it.error }.road

        return buildFillerSurfaceGeometry(laneId, successorLaneId, road, successorRoad)
    }

    /**
     * Returns the geometry of a longitudinal filler surface between the current lane and the successor lane.
     *
     * @param laneId identifier of the lane
     * @param successorLaneId id of the successor lane
     * @param road road to which [laneId] belongs
     * @param successorRoad road to which the successor lane belongs
     */
    private fun buildFillerSurfaceGeometry(laneId: LaneIdentifier, successorLaneId: LaneIdentifier,
                                           road: Road, successorRoad: Road): Optional<AbstractSurface3D> {

        val currentVertices = listOf(road.getLeftLaneBoundary(laneId), road.getRightLaneBoundary(laneId))
                .handleFailure { throw it.error }
                .map { it.calculateEndPointGlobalCS() }
                .handleFailure { throw it.error }

        // false, if the successor lane is connected by it's end (leads to swapping of the vertices)
        val successorContactStart =
                if (laneId.isWithinSameRoad(successorLaneId)) true
                else !(road.linkage.successorContactPoint equalsValue ContactPoint.END)

        val successorLaneBoundaries =
                listOf(successorRoad.getRightLaneBoundary(successorLaneId), successorRoad.getLeftLaneBoundary(successorLaneId))
                        .handleFailure { return Optional.empty() }

        val successorVertices = when (successorContactStart) {
            true -> successorLaneBoundaries.map { it.calculateStartPointGlobalCS() }
            false -> successorLaneBoundaries.map { it.calculateEndPointGlobalCS() }.reversed()
        }.handleAndRemoveFailure { throw it.error }

        val fillerSurfaceVertices = (currentVertices + successorVertices)
                .distinctConsecutiveEnclosing { it }
                .removeLinearlyRedundantVertices()

        return if (fillerSurfaceVertices.size < 3) Optional.empty()
        else Optional(LinearRing3D(fillerSurfaceVertices))
    }

    /**
     * Returns a list of [LaneIdentifier]s that succeed the lane with [laneId] and are located within the same road.
     *
     * @param laneId lane identifier, which must not be located within the last section of the road
     */
    private fun getSuccessorLanesWithinRoad(laneId: LaneIdentifier):
            Result<List<LaneIdentifier>, IllegalArgumentException> {

        val road = roadspaces.getValueResult(laneId.toRoadspaceIdentifier())
                .handleFailure { throw it.error }.road
        check(!road.isInLastLaneSection(laneId))
        { "Current lane must not be located in the last lane section of the road." }

        return road.getLane(laneId)
                .handleFailure { throw it.error }
                .successors
                .map { LaneIdentifier(it, laneId.laneSectionIdentifier.getNextLaneSectionIdentifier()) }
                .let { Result.success(it) }
    }

    /**
     * Returns a list of [LaneIdentifier]s that succeed the lane with [laneId] and are connected via another road.
     *
     * @param laneId lane identifier, which must be located at the last lane section on a road which leads into
     * another road
     */
    private fun getSuccessorLanesBetweenRoads(laneId: LaneIdentifier):
            Result<List<LaneIdentifier>, IllegalArgumentException> {

        val road = roadspaces.getValueResult(laneId.toRoadspaceIdentifier())
                .handleFailure { throw it.error }.road
        check(road.linkage.successorRoadspaceId.isPresent())
        { "Current road must have a successor road." }

        val successorRoad = roadspaces.getValueResult(road.linkage.successorRoadspaceId.getOrNull()!!)
                .handleFailure { return Result.success(emptyList()) }.road
        val laneSectionIdentifier =
                if (road.linkage.successorContactPoint equalsValue ContactPoint.START)
                    successorRoad.laneSections.first().id else successorRoad.laneSections.last().id

        return road.getLane(laneId)
                .handleFailure { throw it.error }
                .successors
                .map { LaneIdentifier(it, laneSectionIdentifier) }
                .let { Result.success(it) }
    }

    /**
     * Returns a list of [LaneIdentifier]s that succeed the lane with [laneId] and are connected via a junction.
     *
     * @param laneId lane identifier, which must be located at the last lane section on a road which leads into
     * a junction
     */
    private fun getSuccessorLanesBetweenRoadsInJunction(laneId: LaneIdentifier):
            Result<List<LaneIdentifier>, IllegalArgumentException> {

        val road = roadspaces.getValueResult(laneId.toRoadspaceIdentifier())
                .handleFailure { throw it.error }.road
        check(road.isInLastLaneSection(laneId))
        { "Current lane must be located in the last lane section of the road." }
        check(road.linkage.successorJunctionId.isPresent())
        { "Current road must have a successor junction." }

        val successorJunction = junctions
                .getValueResult(road.linkage.successorJunctionId.getOrNull()!!)
                .handleFailure { return Result.success(emptyList()) }

        return successorJunction
                .getSuccessorLane(laneId)
                .map { resolveRelativeIdentifier(it) }
                .handleFailure { return Result.success(emptyList()) }
                .let { Result.success(it) }
    }

    /**
     * Resolves [RelativeLaneIdentifier] by looking up the number of lane sections.
     */
    private fun resolveRelativeIdentifier(id: RelativeLaneIdentifier):
            Result<LaneIdentifier, IllegalArgumentException> {

        val road = roadspaces.getValueResult(id.toRoadspaceIdentifier())
                .handleFailure { return it }.road
        return id.toAbsoluteLaneIdentifier(road.numberOfLaneSections()).let { Result.success(it) }
    }

}
