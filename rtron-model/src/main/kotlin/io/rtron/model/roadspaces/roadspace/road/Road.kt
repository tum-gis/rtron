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

import com.github.kittinunf.result.Result
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.SectionedUnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.analysis.function.univariate.pure.ConstantFunction
import io.rtron.math.geometry.curved.threed.surface.AbstractCurveRelativeSurface3D
import io.rtron.math.geometry.curved.threed.surface.SectionedCurveRelativeParametricSurface3D
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.curve.CurveOnParametricSurface3D
import io.rtron.math.geometry.euclidean.threed.point.fuzzyEquals
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.CompositeSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.range.length
import io.rtron.model.roadspaces.roadspace.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.std.getValueResult
import io.rtron.std.handleFailure
import io.rtron.std.isSortedBy

/**
 * Representation of an actual road (without road objects) containing the surface and information on the lane topology
 * as well as attributes.
 *
 * @param id identifier of this road (each roadspace has exactly one road)
 * @param surface surface geometry of the road with torsion applied
 * @param surfaceWithoutTorsion surface geometry of the road without torsion applied
 * @param laneOffset lateral lane offset to road reference line
 * @param laneSections lane sections of this road
 * @param linkage link information to other roads and junctions
 */
class Road(
    val id: RoadspaceIdentifier,
    val surface: AbstractCurveRelativeSurface3D,
    val surfaceWithoutTorsion: AbstractCurveRelativeSurface3D,
    val laneOffset: UnivariateFunction,
    val laneSections: List<LaneSection>,
    val linkage: RoadLinkage
) {

    // Properties and Initializers
    init {
        require(surface.domain == surfaceWithoutTorsion.domain) { "Domains of provided surfaces must have the same domain." }
        require(curvePositionDomain.hasLowerBound()) { "Domain of curve position must have a lower bound." }
        require(curvePositionDomain.hasUpperBound()) { "Domain of curve position must have a upper bound." }
        require(surface.tolerance == surfaceWithoutTorsion.tolerance) { "Surface and SurfaceWithoutTorsion must have the same tolerance." }
        require(laneOffset.domain.fuzzyEncloses(surface.domain, surface.tolerance)) { "The lane offset function must be defined everywhere where the surface is also defined." }
        require(laneSections.isNotEmpty()) { "Road must contain laneSections." }
        require(
            laneSections.mapIndexed { index, laneSection -> index to laneSection }
                .all { it.first == it.second.id.laneSectionId }
        ) { "LaneSection elements must be positioned according to their laneSection id on the list." }

        val expectedLaneIds = laneSections.indices.toList()
        require(laneSections.indices.toList().containsAll(expectedLaneIds)) { "There must be no gaps within the given laneSectionIds." }
        require(laneSections.isSortedBy { it.id.laneSectionId }) { "LaneSections have to be sorted." }

        assert(getLaneSectionCurvePositionDomains().isNotEmpty()) { "The domains of the lane sections must not be empty." }
    }

    /** domain of the curve positions of this road */
    private val curvePositionDomain get() = surface.domain

    /** road surface sectioned into the domains of the lane sections */
    private val sectionedSurfaces: List<AbstractCurveRelativeSurface3D> =
        getLaneSectionCurvePositionDomains().map { SectionedCurveRelativeParametricSurface3D(surface, it) }

    /** road surface without torsion sectioned into the domains of the lane sections */
    private val sectionedSurfacesWithoutTorsion: List<AbstractCurveRelativeSurface3D> =
        getLaneSectionCurvePositionDomains().map { SectionedCurveRelativeParametricSurface3D(surfaceWithoutTorsion, it) }

    /** lateral lane offset function sectioned into the domains of the lane sections */
    private val sectionedLaneOffset: List<UnivariateFunction> =
        getLaneSectionCurvePositionDomains().map { SectionedUnivariateFunction(laneOffset, it) }

    /** tolerance of the used geometries */
    private val geometricalTolerance get() = surface.tolerance

    // Methods

    /**
     * Returns the identifiers of all lanes as a flattened list.
     */
    fun getAllLaneIdentifiers(): List<LaneIdentifier> = laneSections.flatMap { it.lanes.values }.map { it.id }

    /**
     * Returns the identifiers of all lane sections as a list.
     */
    fun getAllLaneSectionIdentifiers(): List<LaneSectionIdentifier> = laneSections.map { it.id }

    /**
     * Returns the lane reference line which is a laterally translated road reference line.
     */
    fun getLaneReferenceLine(): AbstractCurve3D = CurveOnParametricSurface3D.onCompleteSurface(surface, laneOffset)

    /**
     * Returns the lane section with the [laneSectionIdentifier]; if it does not exist, an [Result.Failure] is returned.
     */
    fun getLaneSection(laneSectionIdentifier: LaneSectionIdentifier) =
        laneSections.getValueResult(laneSectionIdentifier.laneSectionId)

    /**
     * Returns an individual lane referenced by [laneIdentifier]; if it does not exist, an [Result.Failure] is returned.
     */
    fun getLane(laneIdentifier: LaneIdentifier): Result<Lane, IllegalArgumentException> =
        getLaneSection(laneIdentifier.laneSectionIdentifier)
            .handleFailure { return it }
            .getLane(laneIdentifier.laneId)

    /**
     * Returns all lane surfaces contained in this road.
     *
     * @param step discretization step size
     * @return a triple of the lane identifier, the lane surface geometry and the lane's attribute list
     */
    fun getAllLanes(step: Double): List<Result<Triple<LaneIdentifier, AbstractSurface3D, AttributeList>, Exception>> =
        getAllLaneIdentifiers().map { id ->
            val laneSurface = getLaneSurface(id, step).handleFailure { return@map it }
            val attributes = getAttributeList(id).handleFailure { return@map it }
            Result.success(Triple(id, laneSurface, attributes))
        }

    /**
     * Returns the center line of the road
     */
    fun getAllCenterLanes(): List<Triple<LaneIdentifier, AbstractCurve3D, AttributeList>> =
        laneSections.map { it.centerLane }.map { element ->
            val line = getCurveOnLaneSectionSurface(element.id.laneSectionIdentifier, element.level)
                .handleFailure { throw it.error }
            Triple(element.id, line, element.attributes)
        }

    /**
     * Returns the left boundary of all lanes contained in this road.
     *
     * @return a triple of the lane identifier, the curve geometry and the lane's id attribute list
     */
    fun getAllLeftLaneBoundaries(): List<Pair<LaneIdentifier, AbstractCurve3D>> =
        getAllLaneIdentifiers().map { id ->
            val curve = getLeftLaneBoundary(id).handleFailure { throw it.error }
            Pair(id, curve)
        }

    /**
     * Returns the right boundary of all lanes contained in this road.
     *
     * @return a triple of the lane identifier, the curve geometry and the lane's id attribute list
     */
    fun getAllRightLaneBoundaries(): List<Pair<LaneIdentifier, AbstractCurve3D>> =
        getAllLaneIdentifiers().map { id ->
            val curve = getRightLaneBoundary(id).handleFailure { throw it.error }
            Pair(id, curve)
        }

    /**
     * Returns a lane curve for all lanes contained in this road.
     *
     * @param factor if the factor is 0.0, the inner lane boundary is returned; if the factor is 1.0, the outer
     * lane boundary is returned; if the factor is 0.5, the center line of the lane is returned
     * @return a triple of the lane identifier, the curve geometry and the lane's id attribute list
     */
    fun getAllCurvesOnLanes(factor: Double): List<Pair<LaneIdentifier, AbstractCurve3D>> =
        getAllLaneIdentifiers().map { id ->
            val curve = getCurveOnLane(id, factor).handleFailure { throw it.error }
            Pair(id, curve)
        }

    /**
     * Returns all lateral filler surfaces of this road.
     *
     * @param step discretization step size
     * @return lane identifier and the filler surface to the left of the respective lane
     */
    fun getAllLateralFillerSurfaces(step: Double): List<Pair<LaneIdentifier, AbstractSurface3D>> =
        getAllLaneIdentifiers().fold(emptyList()) { acc, id ->
            val fillerSurface = getLeftLateralFillerSurfaceOrNull(id, step)
                .handleFailure { throw it.error }
            if (fillerSurface == null) acc else acc + Pair(id, fillerSurface)
        }

    /**
     * Returns all road markings of all lanes and center lanes.
     *
     * @param step discretization step size
     * @return list of the road markings containing it's identifier, geometry and attribute list
     */
    fun getAllRoadMarkings(step: Double):
        List<Result<Triple<LaneIdentifier, AbstractGeometry3D, AttributeList>, Exception>> =
            getAllLaneIdentifiers().flatMap { getRoadMarkings(it, step) } +
                getAllLaneSectionIdentifiers().flatMap { getCenterRoadMarkings(it, step) }

    /**
     * Returns the left boundary of an individual lane with [laneIdentifier].
     */
    fun getLeftLaneBoundary(laneIdentifier: LaneIdentifier): Result<AbstractCurve3D, Exception> =
        if (laneIdentifier.isLeft()) getCurveOnLane(laneIdentifier, 1.0)
        else getCurveOnLane(laneIdentifier, 0.0)

    /**
     * Returns the right boundary of an individual lane with [laneIdentifier].
     */
    fun getRightLaneBoundary(laneIdentifier: LaneIdentifier): Result<AbstractCurve3D, Exception> =
        if (laneIdentifier.isLeft()) getCurveOnLane(laneIdentifier, 0.0)
        else getCurveOnLane(laneIdentifier, 1.0)

    /**
     * Returns a curve that lies on the road surface and is parallel to the lane boundaries
     *
     * @param laneIdentifier identifier for requested lane
     * @param factor if the factor is 0.0, the inner lane boundary is returned; if the factor is 1.0, the outer
     * lane boundary is returned; if the factor is 0.5, the center line of the lane is returned
     */
    private fun getCurveOnLane(
        laneIdentifier: LaneIdentifier,
        factor: Double,
        addLateralOffset: UnivariateFunction = ConstantFunction.ZERO
    ):
        Result<AbstractCurve3D, Exception> {

            // select the requested lane
            val selectedLaneSection = getLaneSection(laneIdentifier.laneSectionIdentifier)
                .handleFailure { return it }
            val selectedLane = selectedLaneSection.getLane(laneIdentifier.laneId)
                .handleFailure { return it }

            // select the correct surface and section it
            val sectionedSurface =
                if (selectedLane.level) sectionedSurfacesWithoutTorsion[laneIdentifier.laneSectionId]
                else sectionedSurfaces[laneIdentifier.laneSectionId]

            // calculate the total lateral offset function to the road's reference line
            val sectionedLaneReferenceOffset = sectionedLaneOffset[laneIdentifier.laneSectionId]
            val lateralLaneOffset = selectedLaneSection
                .getLateralLaneOffset(laneIdentifier.laneId, factor)
                .handleFailure { return it }
            val lateralOffset = StackedFunction.ofSum(
                sectionedLaneReferenceOffset,
                lateralLaneOffset,
                addLateralOffset
            )

            // calculate the additional height offset for the specific factor
            val heightLaneOffset = selectedLaneSection
                .getLaneHeightOffset(laneIdentifier, factor)
                .handleFailure { return it }

            // combine it to a curve on the sectioned road surface
            val curveOnLane = CurveOnParametricSurface3D(sectionedSurface, lateralOffset, heightLaneOffset)
            return Result.success(curveOnLane)
        }

    private fun getCurveOnLaneSectionSurface(
        laneSectionIdentifier: LaneSectionIdentifier,
        level: Boolean,
        addLateralOffset: UnivariateFunction = ConstantFunction.ZERO
    ):
        Result<AbstractCurve3D, Exception> {

            // select the correct surface and section it
            val sectionedSurface =
                if (level) sectionedSurfacesWithoutTorsion[laneSectionIdentifier.laneSectionId]
                else sectionedSurfaces[laneSectionIdentifier.laneSectionId]

            // calculate the total lateral offset function to the road's reference line
            val sectionedLaneReferenceOffset = sectionedLaneOffset[laneSectionIdentifier.laneSectionId]
            val lateralOffset = StackedFunction.ofSum(sectionedLaneReferenceOffset, addLateralOffset)

            val curveOnLane = CurveOnParametricSurface3D(sectionedSurface, lateralOffset)
            return Result.success(curveOnLane)
        }

    /**
     * Returns the surface of an individual lane with [laneIdentifier] and a certain discretization [step] size.
     */
    fun getLaneSurface(laneIdentifier: LaneIdentifier, step: Double): Result<AbstractSurface3D, Exception> {

        val laneSection = getLaneSection(laneIdentifier.laneSectionIdentifier)
            .handleFailure { throw it.error }
        if (laneSection.curvePositionDomain.length < geometricalTolerance)
            return Result.error(
                IllegalStateException(
                    "$laneIdentifier: The length of the lane is almost zero " +
                        "(below tolerance) and thus no surface can be constructed."
                )
            )

        val leftBoundary = getLeftLaneBoundary(laneIdentifier)
            .handleFailure { return it }
            .calculatePointListGlobalCS(step)
            .handleFailure { throw it.error }
        val rightBoundary = getRightLaneBoundary(laneIdentifier)
            .handleFailure { throw it.error }
            .calculatePointListGlobalCS(step)
            .handleFailure { throw it.error }

        if (leftBoundary.zip(rightBoundary).all { it.first.fuzzyEquals(it.second, geometricalTolerance) })
            return Result.error(
                IllegalStateException(
                    "$laneIdentifier: Lane has zero width (when discretized) and " +
                        "thus no surface can be constructed."
                )
            )

        val surface =
            LinearRing3D.ofWithDuplicatesRemoval(leftBoundary, rightBoundary, geometricalTolerance)
                .handleFailure { return it }
                .let { CompositeSurface3D(it) }
        return Result.success(surface)
    }

    /**
     * Returns true, if the lane with [laneIdentifier] is contained in the last lane section of the road.
     */
    fun isInFirstLaneSection(laneIdentifier: LaneIdentifier) =
        laneSections.first().id == laneIdentifier.laneSectionIdentifier

    /**
     * Returns true, if the lane with [laneIdentifier] is contained in the last lane section of the road.
     */
    fun isInLastLaneSection(laneIdentifier: LaneIdentifier) =
        laneSections.last().id == laneIdentifier.laneSectionIdentifier

    /**
     * Returns the number of contained lane sections.
     */
    fun numberOfLaneSections(): Int = laneSections.size

    /**
     * Returns the filler surface which closes the gap occurring at the lateral transition of two lane elements.
     * These lateral transitions might contain vertical holes which are caused by e.g. lane height offsets.
     * If no lateral surface filler is needed due to adjacent lane surfaces, null is returned.
     *
     * @param laneIdentifier lane identifier for which the lateral filler surfaces to the left shall be created
     * @param step discretization step size
     */
    private fun getLeftLateralFillerSurfaceOrNull(laneIdentifier: LaneIdentifier, step: Double):
        Result<AbstractSurface3D?, Exception> {

            // return no lateral filler surface, if there is no lane on the left
            getLane(laneIdentifier.getAdjacentLeftLaneIdentifier())
                .handleFailure { return Result.success(null) }

            val leftLaneBoundary = getLeftLaneBoundary(laneIdentifier)
                .handleFailure { return it }
                .calculatePointListGlobalCS(step)
                .handleFailure { return it }
            val rightLaneBoundary = getRightLaneBoundary(laneIdentifier.getAdjacentLeftLaneIdentifier())
                .handleFailure { return it }
                .calculatePointListGlobalCS(step)
                .handleFailure { return it }

            // return no lateral filler surface, if there is no gap between the lane surfaces
            if (leftLaneBoundary.fuzzyEquals(rightLaneBoundary))
                return Result.success(null)

            return LinearRing3D.ofWithDuplicatesRemoval(rightLaneBoundary, leftLaneBoundary, geometricalTolerance)
                .handleFailure { return it }
                .let { CompositeSurface3D(it) }
                .let { Result.success(it) }
        }

    /**
     * Returns all road markings of the center lane of the lane section with [laneSectionIdentifier].
     *
     * @param laneSectionIdentifier identifier for which the road markings shall be returned
     * @param step discretization step size
     */
    private fun getCenterRoadMarkings(laneSectionIdentifier: LaneSectionIdentifier, step: Double):
        List<Result<Triple<LaneIdentifier, AbstractGeometry3D, AttributeList>, Exception>> {

            val centerLane = getLaneSection(laneSectionIdentifier).handleFailure { throw it.error }.centerLane

            return centerLane.roadMarkings.map {
                Result.success(Triple(centerLane.id, getCenterRoadMarkingGeometry(centerLane, it, step), it.attributes))
            }
        }

    /**
     * Returns the geometry of a [roadMarking] which is attached to a [centerLane].
     *
     * @param centerLane center lane for which the road markings shall be returned
     * @param roadMarking road marking for which the geometry shall be returned
     * @return either a [AbstractCurve3D], if the road marking has zero width, or a [AbstractSurface3D], if the width
     * is not zero
     */
    private fun getCenterRoadMarkingGeometry(centerLane: CenterLane, roadMarking: RoadMarking, step: Double):
        AbstractGeometry3D {

            if (roadMarking.width.value < geometricalTolerance)
                return getCurveOnLaneSectionSurface(centerLane.id.laneSectionIdentifier, centerLane.level)
                    .handleFailure { throw it.error }

            val leftOffsetFunction = roadMarking.width timesValue 0.5
            val leftRoadMarkingBoundary =
                getCurveOnLaneSectionSurface(centerLane.id.laneSectionIdentifier, centerLane.level, leftOffsetFunction)
                    .handleFailure { throw it.error }.calculatePointListGlobalCS(step).handleFailure { throw it.error }
            val rightOffsetFunction = roadMarking.width timesValue -0.5
            val rightRoadMarkingBoundary =
                getCurveOnLaneSectionSurface(centerLane.id.laneSectionIdentifier, centerLane.level, rightOffsetFunction)
                    .handleFailure { throw it.error }.calculatePointListGlobalCS(step).handleFailure { throw it.error }

            return LinearRing3D.ofWithDuplicatesRemoval(leftRoadMarkingBoundary, rightRoadMarkingBoundary, geometricalTolerance)
                .handleFailure { throw it.error }
                .let { CompositeSurface3D(it) }
        }

    /**
     * Returns all road markings of a lane with [laneIdentifier].
     *
     * @param laneIdentifier lane identifier for which the road markings shall be returned
     * @param step discretization step size
     */
    private fun getRoadMarkings(laneIdentifier: LaneIdentifier, step: Double):
        List<Result<Triple<LaneIdentifier, AbstractGeometry3D, AttributeList>, Exception>> =
            getLane(laneIdentifier)
                .handleFailure { throw it.error }
                .roadMarkings
                .map { currentRoadMarking ->
                    val geometry = getRoadMarkingGeometry(laneIdentifier, currentRoadMarking, step)
                        .handleFailure { return@map it }
                    Result.success(Triple(laneIdentifier, geometry, currentRoadMarking.attributes))
                }

    /**
     * Returns the geometry of a [roadMarking] which is attached to a lane with [laneIdentifier].
     *
     * @param laneIdentifier identifier of the lane to which the [roadMarking] belongs
     * @param roadMarking road marking for which the geometry shall be returned
     * @return either a [AbstractCurve3D], if the road marking has zero width, or a [AbstractSurface3D], if the width
     * is not zero
     */
    private fun getRoadMarkingGeometry(laneIdentifier: LaneIdentifier, roadMarking: RoadMarking, step: Double):
        Result<AbstractGeometry3D, Exception> {

            if (roadMarking.width.domain.length < geometricalTolerance)
                return Result.error(
                    IllegalStateException(
                        "$laneIdentifier: Road marking's length is almost zero " +
                            "(below tolerance) and thus no surface can be constructed."
                    )
                )

            if (roadMarking.width.value < geometricalTolerance)
                return getCurveOnLane(laneIdentifier, 1.0, roadMarking.width)

            val leftOffsetFunction = roadMarking.width timesValue 0.5
            val leftRoadMarkBoundary = getCurveOnLane(laneIdentifier, 1.0, leftOffsetFunction)
                .handleFailure { throw it.error }.calculatePointListGlobalCS(step).handleFailure { throw it.error }
            val rightOffsetFunction = roadMarking.width timesValue -0.5
            val rightRoadMarkBoundary = getCurveOnLane(laneIdentifier, 1.0, rightOffsetFunction)
                .handleFailure { throw it.error }.calculatePointListGlobalCS(step).handleFailure { throw it.error }

            return LinearRing3D.ofWithDuplicatesRemoval(leftRoadMarkBoundary, rightRoadMarkBoundary, geometricalTolerance)
                .handleFailure { throw it.error }
                .let { CompositeSurface3D(it) }
                .let { Result.success(it) }
        }

    /**
     * Returns the curve position domains of each lane section.
     */
    private fun getLaneSectionCurvePositionDomains(): List<Range<Double>> {
        val laneSectionDomains = laneSections
            .map { it.curvePositionStart.curvePosition }
            .zipWithNext()
            .map { Range.closed(it.first, it.second) }

        val lastLaneSectionDomain = Range.closedX(
            laneSections.last().curvePositionStart.curvePosition,
            curvePositionDomain.upperEndpointOrNull()!!,
            curvePositionDomain.upperBoundType()
        )

        return laneSectionDomains + lastLaneSectionDomain
    }

    private fun getAttributeList(laneIdentifier: LaneIdentifier): Result<AttributeList, IllegalArgumentException> =
        getLane(laneIdentifier)
            .handleFailure { return it }
            .attributes
            .let { Result.success(it) }
}
