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

package io.rtron.model.roadspaces.roadspace.road

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import io.rtron.math.analysis.function.univariate.UnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.SectionedUnivariateFunction
import io.rtron.math.analysis.function.univariate.combination.StackedFunction
import io.rtron.math.analysis.function.univariate.pure.ConstantFunction
import io.rtron.math.geometry.curved.oned.point.CurveRelativeVector1D
import io.rtron.math.geometry.curved.threed.surface.AbstractCurveRelativeSurface3D
import io.rtron.math.geometry.curved.threed.surface.SectionedCurveRelativeParametricSurface3D
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.curve.CurveOnParametricSurface3D
import io.rtron.math.geometry.euclidean.threed.point.fuzzyEquals
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.CompositeSurface3D
import io.rtron.math.geometry.euclidean.threed.surface.LinearRing3D
import io.rtron.math.geometry.toIllegalStateException
import io.rtron.math.range.BoundType
import io.rtron.math.range.Range
import io.rtron.math.range.fuzzyContains
import io.rtron.math.range.fuzzyEncloses
import io.rtron.math.range.length
import io.rtron.model.roadspaces.common.LateralFillerSurface
import io.rtron.model.roadspaces.identifier.JunctionIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.LaneSectionIdentifier
import io.rtron.model.roadspaces.identifier.LateralLaneRangeIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.roadspace.ContactPoint
import io.rtron.model.roadspaces.roadspace.RoadspaceContactPointIdentifier
import io.rtron.model.roadspaces.roadspace.attribute.AttributeList
import io.rtron.std.getValueEither
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
    val laneSections: NonEmptyList<LaneSection>,
    val linkage: RoadLinkage,
) {
    // Properties and Initializers
    init {
        require(surface.domain == surfaceWithoutTorsion.domain) { "Domains of provided surfaces must have the same domain." }
        require(curvePositionDomain.hasLowerBound()) { "Domain of curve position must have a lower bound." }
        require(curvePositionDomain.hasUpperBound()) { "Domain of curve position must have a upper bound." }
        require(surface.tolerance == surfaceWithoutTorsion.tolerance) { "Surface and SurfaceWithoutTorsion must have the same tolerance." }
        require(
            laneOffset.domain.fuzzyEncloses(
                surface.domain,
                surface.tolerance,
            ),
        ) { "The lane offset function must be defined everywhere where the surface is also defined." }
        require(
            laneSections.mapIndexed { index, laneSection -> index to laneSection }
                .all { it.first == it.second.id.laneSectionId },
        ) { "LaneSection elements must be positioned according to their laneSection id on the list." }
        require(
            laneSections.dropLast(1)
                .all {
                    it.curvePositionDomain.lowerBoundType() == BoundType.CLOSED &&
                        it.curvePositionDomain.upperBoundType() == BoundType.OPEN
                },
        ) { "CurvePositionDomain of all LaneSections apart from the last must have a closed lower and open upper bound." }
        require(
            laneSections.last().curvePositionDomain.lowerBoundType() == BoundType.CLOSED &&
                laneSections.last().curvePositionDomain.upperBoundType() == BoundType.CLOSED,
        ) {
            "CurvePositionDomain of the last LaneSection must have a closed lower and upper bound."
        }

        val expectedLaneIds = laneSections.indices.toList()
        require(
            laneSections.indices.toList().containsAll(expectedLaneIds),
        ) { "There must be no gaps within the given laneSectionIds." }
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
        getLaneSectionCurvePositionDomains().map {
            SectionedCurveRelativeParametricSurface3D(
                surfaceWithoutTorsion,
                it,
            )
        }

    /** lateral lane offset function sectioned into the domains of the lane sections */
    private val sectionedLaneOffset: List<UnivariateFunction> =
        getLaneSectionCurvePositionDomains().map { SectionedUnivariateFunction(laneOffset, it) }

    /** tolerance of the used geometries */
    private val geometricalTolerance get() = surface.tolerance

    // Methods

    /** Returns the identifiers of all left and right lanes as a flattened list. */
    fun getAllLeftRightLaneIdentifiers(): List<LaneIdentifier> = getAllLeftRightLanes().map { it.id }

    /** Returns all left and right lanes as a flattened list. */
    fun getAllLeftRightLanes(): List<Lane> = laneSections.toList().flatMap { it.lanes.values }

    /** Returns the identifiers of all center lanes as a list. */
    fun getAllCenterLaneIdentifier(): List<LaneIdentifier> = laneSections.map { it.centerLane.id }

    /** Returns the identifiers of all lanes (center, left and right) as a list. */
    fun getAllLaneIdentifiers(): List<LaneIdentifier> = getAllCenterLaneIdentifier() + getAllLeftRightLaneIdentifiers()

    /** Returns the identifiers of all lane sections as a list. */
    fun getAllLaneSectionIdentifiers(): List<LaneSectionIdentifier> = laneSections.map { it.id }

    /** Returns the lane reference line which is a laterally translated road reference line. */
    fun getLaneReferenceLine(): AbstractCurve3D = CurveOnParametricSurface3D.onCompleteSurface(surface, laneOffset)

    /** Returns the lane section with the [laneSectionIdentifier]; if it does not exist, an [Either.Left] is returned. */
    fun getLaneSection(laneSectionIdentifier: LaneSectionIdentifier): Either<IllegalArgumentException, LaneSection> =
        laneSections.getValueEither(laneSectionIdentifier.laneSectionId).mapLeft { it.toIllegalArgumentException() }

    /** Returns the lane section of the [curveRelativePoint]; if it does not exist, an [Either.Left] is returned. */
    fun getLaneSection(curveRelativePoint: CurveRelativeVector1D): Either<IllegalArgumentException, LaneSection> {
        val selectedLaneSections =
            laneSections.filter { it.curvePositionDomain.contains(curveRelativePoint.curvePosition) }
        if (selectedLaneSections.size == 1) {
            return selectedLaneSections.first().right()
        } else if (selectedLaneSections.size > 1) {
            throw IllegalArgumentException("Domains of lane sections must close flush.")
        }

        val fuzzySelectedLaneSections =
            laneSections.filter {
                it.curvePositionDomain.fuzzyContains(
                    curveRelativePoint.curvePosition,
                    geometricalTolerance,
                )
            }
        return when (fuzzySelectedLaneSections.size) {
            1 -> fuzzySelectedLaneSections.first().right()
            0 -> IllegalArgumentException("No laneSection found").left()
            else -> throw IllegalArgumentException("Domains of lane sections must close flush.")
        }
    }

    /** Returns an individual lane referenced by [laneIdentifier]; if it does not exist, an [Either.Left] is returned. */
    fun getLane(laneIdentifier: LaneIdentifier): Either<IllegalArgumentException, Lane> =
        either {
            val laneSection = getLaneSection(laneIdentifier.laneSectionIdentifier).bind()
            laneSection.getLane(laneIdentifier.laneId).bind()
        }

    /** Returns true, if road belongs to a junction. */
    fun isLocatedInJunction() = linkage.belongsToJunctionId.isSome()

    /** Returns the contact point of the roadspace which connects to the junction with the [junctionIdentifier]. */
    fun getRoadspaceContactPointToJunction(junctionIdentifier: JunctionIdentifier): Option<RoadspaceContactPointIdentifier> =
        when {
            linkage.predecessorJunctionId.isSome { it == junctionIdentifier } ->
                Some(
                    RoadspaceContactPointIdentifier(
                        ContactPoint.START,
                        id,
                    ),
                )

            linkage.successorJunctionId.isSome { it == junctionIdentifier } ->
                Some(
                    RoadspaceContactPointIdentifier(
                        ContactPoint.END,
                        id,
                    ),
                )

            else -> None
        }

    /** Returns the [LaneSectionIdentifier] (first or last lane section) of the roadspace which is referenced by the
     * [roadspaceContactPointIdentifier]. */
    fun getLaneSectionIdentifier(roadspaceContactPointIdentifier: RoadspaceContactPointIdentifier): LaneSectionIdentifier {
        require(roadspaceContactPointIdentifier.roadspaceIdentifier == id) {
            "RoadspaceContactIdentifier ($roadspaceContactPointIdentifier) must reference this road (id=$id)."
        }

        return when (roadspaceContactPointIdentifier.roadspaceContactPoint) {
            ContactPoint.START -> laneSections.first().id
            ContactPoint.END -> laneSections.last().id
        }
    }

    /** Returns true, if the lane with [laneIdentifier] is contained in the last lane section of the road. */
    fun isInFirstLaneSection(laneIdentifier: LaneIdentifier) = laneIdentifier.laneSectionIdentifier == laneSections.first().id

    /** Returns true, if the lane with [laneIdentifier] is contained in the last lane section of the road. */
    fun isInLastLaneSection(laneIdentifier: LaneIdentifier) = laneIdentifier.laneSectionIdentifier == laneSections.last().id

    /** Returns the number of contained lane sections. */
    fun numberOfLaneSections(): Int = laneSections.size

    /** Returns the center line of the road. */
    fun getAllCenterLanes(): List<Triple<LaneIdentifier, AbstractCurve3D, AttributeList>> =
        laneSections.map { it.centerLane }.map { element ->
            val line =
                getCurveOnLaneSectionSurface(element.id.laneSectionIdentifier, element.level).getOrElse { throw it }
            Triple(element.id, line, element.attributes)
        }

    /**
     * Returns the left boundary of all lanes contained in this road.
     *
     * @return a triple of the lane identifier, the curve geometry and the lane's id attribute list
     */
    fun getAllLeftLaneBoundaries(): List<Pair<LaneIdentifier, AbstractCurve3D>> =
        getAllLeftRightLaneIdentifiers().map { id ->
            val curve = getLeftLaneBoundary(id).getOrElse { throw it }
            Pair(id, curve)
        }

    /**
     * Returns the right boundary of all lanes contained in this road.
     *
     * @return a triple of the lane identifier, the curve geometry and the lane's id attribute list
     */
    fun getAllRightLaneBoundaries(): List<Pair<LaneIdentifier, AbstractCurve3D>> =
        getAllLeftRightLaneIdentifiers().map { id ->
            val curve = getRightLaneBoundary(id).getOrElse { throw it }
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
        getAllLeftRightLaneIdentifiers().map { id ->
            val curve = getCurveOnLane(id, factor).getOrElse { throw it }
            Pair(id, curve)
        }

    /** Returns the left boundary of an individual lane with [laneIdentifier]. */
    fun getLeftLaneBoundary(laneIdentifier: LaneIdentifier): Either<Exception, AbstractCurve3D> =
        if (laneIdentifier.isLeft()) {
            getOuterLaneBoundary(laneIdentifier)
        } else {
            getInnerLaneBoundary(laneIdentifier)
        }

    /** Returns the right boundary of an individual lane with [laneIdentifier]. */
    fun getRightLaneBoundary(laneIdentifier: LaneIdentifier): Either<Exception, AbstractCurve3D> =
        if (laneIdentifier.isLeft()) {
            getInnerLaneBoundary(laneIdentifier)
        } else {
            getOuterLaneBoundary(laneIdentifier)
        }

    /** Returns the inner lane boundary of an individual lane with [laneIdentifier]. */
    fun getInnerLaneBoundary(laneIdentifier: LaneIdentifier): Either<Exception, AbstractCurve3D> = getCurveOnLane(laneIdentifier, 0.0)

    /** Returns the outer lane boundary of an individual lane with [laneIdentifier]. */
    fun getOuterLaneBoundary(laneIdentifier: LaneIdentifier): Either<Exception, AbstractCurve3D> = getCurveOnLane(laneIdentifier, 1.0)

    /** Returns the curve of the center lane with [laneSectionIdentifier]. */
    fun getCurveOfCenterLane(laneSectionIdentifier: LaneSectionIdentifier): Either<Exception, AbstractCurve3D> =
        either {
            val laneSection = getLaneSection(laneSectionIdentifier).bind()

            val line =
                getCurveOnLaneSectionSurface(laneSectionIdentifier, laneSection.centerLane.level).getOrElse { throw it }
            line
        }

    /**
     * Returns a curve that lies on the road surface and is parallel to the lane boundaries
     *
     * @param laneIdentifier identifier for requested lane
     * @param factor if the factor is 0.0, the inner lane boundary is returned; if the factor is 1.0, the outer
     * lane boundary is returned; if the factor is 0.5, the center line of the lane is returned
     */
    fun getCurveOnLane(
        laneIdentifier: LaneIdentifier,
        factor: Double,
        addLateralOffset: UnivariateFunction = ConstantFunction.ZERO,
    ): Either<Exception, AbstractCurve3D> =
        either {
            require(laneIdentifier.isLeft() || laneIdentifier.isRight()) { "Identifier of lane must represent a left or a right lane." }

            // select the requested lane
            val selectedLaneSection = getLaneSection(laneIdentifier.laneSectionIdentifier).bind()
            val selectedLane = selectedLaneSection.getLane(laneIdentifier.laneId).bind()

            // select the correct surface and section it
            val sectionedSurface =
                if (selectedLane.level) {
                    sectionedSurfacesWithoutTorsion[laneIdentifier.laneSectionId]
                } else {
                    sectionedSurfaces[laneIdentifier.laneSectionId]
                }

            // calculate the total lateral offset function to the road's reference line
            val sectionedLaneReferenceOffset = sectionedLaneOffset[laneIdentifier.laneSectionId]
            val lateralLaneOffset =
                selectedLaneSection
                    .getLateralLaneOffset(laneIdentifier.laneId, factor)
                    .bind()
            val lateralOffset =
                StackedFunction.ofSum(
                    sectionedLaneReferenceOffset,
                    lateralLaneOffset,
                    addLateralOffset,
                )

            // calculate the additional height offset for the specific factor
            val heightLaneOffset =
                selectedLaneSection
                    .getLaneHeightOffset(laneIdentifier, factor)
                    .bind()

            // combine it to a curve on the sectioned road surface
            CurveOnParametricSurface3D(sectionedSurface, lateralOffset, heightLaneOffset)
        }

    private fun getCurveOnLaneSectionSurface(
        laneSectionIdentifier: LaneSectionIdentifier,
        level: Boolean,
        addLateralOffset: UnivariateFunction = ConstantFunction.ZERO,
    ): Either<Exception, AbstractCurve3D> {
        // select the correct surface and section it
        val sectionedSurface =
            if (level) {
                sectionedSurfacesWithoutTorsion[laneSectionIdentifier.laneSectionId]
            } else {
                sectionedSurfaces[laneSectionIdentifier.laneSectionId]
            }

        // calculate the total lateral offset function to the road's reference line
        val sectionedLaneReferenceOffset = sectionedLaneOffset[laneSectionIdentifier.laneSectionId]
        val lateralOffset = StackedFunction.ofSum(sectionedLaneReferenceOffset, addLateralOffset)

        val curveOnLane = CurveOnParametricSurface3D(sectionedSurface, lateralOffset)
        return Either.Right(curveOnLane)
    }

    /**
     * Returns the surface of an individual lane with [laneIdentifier] and a certain discretization [step] size.
     */
    fun getLaneSurface(
        laneIdentifier: LaneIdentifier,
        step: Double,
    ): Either<Exception, AbstractSurface3D> =
        either {
            val laneSection =
                getLaneSection(laneIdentifier.laneSectionIdentifier)
                    .getOrElse { throw it }
            if (laneSection.curvePositionDomain.length < geometricalTolerance) {
                Either.Left(
                    IllegalStateException(
                        "${laneIdentifier.toIdentifierText()}: The length of the lane is almost zero " +
                            "(below tolerance) and thus no surface can be constructed.",
                    ),
                ).bind<AbstractSurface3D>()
            }

            val leftBoundary =
                getLeftLaneBoundary(laneIdentifier)
                    .getOrElse { throw it }
                    .calculatePointListGlobalCS(step)
                    .mapLeft { it.toIllegalStateException() }
                    .getOrElse { throw it }
            val rightBoundary =
                getRightLaneBoundary(laneIdentifier)
                    .getOrElse { throw it }
                    .calculatePointListGlobalCS(step)
                    .mapLeft { it.toIllegalStateException() }
                    .getOrElse { throw it }

            if (leftBoundary.zip(rightBoundary).all { it.first.fuzzyEquals(it.second, geometricalTolerance) }) {
                Either.Left(
                    IllegalStateException(
                        "${laneIdentifier.toIdentifierText()}: Lane has zero width (when discretized) and " +
                            "thus no surface can be constructed.",
                    ),
                ).bind<AbstractSurface3D>()
            }

            val surface =
                LinearRing3D.ofWithDuplicatesRemoval(leftBoundary, rightBoundary, geometricalTolerance)
                    .mapLeft { IllegalStateException(it.message) }
                    .bind()
                    .let { CompositeSurface3D(it) }
            surface
        }

    /**
     * Returns the filler surface which closes the gap occurring at the lateral transition of the [laneIdentifier] to
     * the inner lane toward the road reference line.
     * These lateral transitions might contain vertical holes which are caused by e.g. lane height offsets.
     * If no lateral surface filler is needed due to adjacent lane surfaces, [Option<Nothing>] is returned.
     *
     * @param laneIdentifier lane identifier for which the lateral filler surfaces to the left shall be created
     * @param step discretization step size
     */
    fun getLateralFillerSurface(
        laneIdentifier: LaneIdentifier,
        step: Double,
    ): Either<Exception, Option<LateralFillerSurface>> =
        either {
            require(laneIdentifier.isLeft() || laneIdentifier.isRight()) { "Identifier of lane must represent a left or a right lane." }

            val innerLaneBoundaryOfThisLaneSampled =
                getInnerLaneBoundary(laneIdentifier).bind()
                    .calculatePointListGlobalCS(step)
                    .mapLeft { it.toIllegalStateException() }
                    .bind()

            val innerLaneIdentifier = laneIdentifier.getAdjacentInnerLaneIdentifier()

            val outerLaneBoundaryOfInnerLane =
                if (innerLaneIdentifier.isCenter()) {
                    getCurveOfCenterLane(innerLaneIdentifier.laneSectionIdentifier)
                } else {
                    getOuterLaneBoundary(
                        innerLaneIdentifier,
                    )
                }
            val outerLaneBoundaryOfInnerLaneSampled =
                outerLaneBoundaryOfInnerLane.bind()
                    .calculatePointListGlobalCS(step)
                    .mapLeft { it.toIllegalStateException() }
                    .bind()

            // return no lateral filler surface, if there is no gap between the lane surfaces
            if (innerLaneBoundaryOfThisLaneSampled.fuzzyEquals(outerLaneBoundaryOfInnerLaneSampled, geometricalTolerance)) {
                return@either None
            }

            val leftLaneBoundary =
                if (laneIdentifier.isLeft()) outerLaneBoundaryOfInnerLaneSampled else innerLaneBoundaryOfThisLaneSampled
            val rightLaneBoundary =
                if (laneIdentifier.isLeft()) innerLaneBoundaryOfThisLaneSampled else outerLaneBoundaryOfInnerLaneSampled

            LinearRing3D.ofWithDuplicatesRemoval(rightLaneBoundary, leftLaneBoundary, geometricalTolerance)
                .mapLeft { IllegalStateException(it.message) }
                .bind()
                .let { CompositeSurface3D(it) }
                .let { LateralFillerSurface(LateralLaneRangeIdentifier.of(laneIdentifier, innerLaneIdentifier), it) }
                .let { Some(it) }
        }

    fun getRoadMarkings(
        laneIdentifier: LaneIdentifier,
        step: Double,
    ): List<Either<Exception, Pair<RoadMarking, AbstractGeometry3D>>> =
        if (laneIdentifier.isCenter()) {
            getCenterRoadMarkings(laneIdentifier, step)
        } else {
            getLeftRightRoadMarkings(
                laneIdentifier,
                step,
            )
        }

    /**
     * Returns all road markings of the center lane of the lane section with [LaneIdentifier].
     *
     * @param laneIdentifier identifier of lane (must represent a center lane) for which the road markings shall be returned
     * @param step discretization step size
     */
    private fun getCenterRoadMarkings(
        laneIdentifier: LaneIdentifier,
        step: Double,
    ): List<Either<Exception, Pair<RoadMarking, AbstractGeometry3D>>> {
        require(laneIdentifier.isCenter()) { "Identifier of lane must represent a center lane." }

        val centerLane = getLaneSection(laneIdentifier.laneSectionIdentifier).getOrElse { throw it }.centerLane

        return centerLane.roadMarkings.map {
            Either.Right(it to getCenterRoadMarkingGeometry(centerLane, it, step))
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
    private fun getCenterRoadMarkingGeometry(
        centerLane: CenterLane,
        roadMarking: RoadMarking,
        step: Double,
    ): AbstractGeometry3D {
        if (roadMarking.width.value < geometricalTolerance) {
            return getCurveOnLaneSectionSurface(centerLane.id.laneSectionIdentifier, centerLane.level)
                .getOrElse { throw it }
        }

        val leftOffsetFunction = roadMarking.width timesValue 0.5
        val leftRoadMarkingBoundary =
            getCurveOnLaneSectionSurface(centerLane.id.laneSectionIdentifier, centerLane.level, leftOffsetFunction)
                .getOrElse { throw it }
                .calculatePointListGlobalCS(step)
                .mapLeft { it.toIllegalStateException() }
                .getOrElse { throw it }
        val rightOffsetFunction = roadMarking.width timesValue -0.5
        val rightRoadMarkingBoundary =
            getCurveOnLaneSectionSurface(centerLane.id.laneSectionIdentifier, centerLane.level, rightOffsetFunction)
                .getOrElse { throw it }
                .calculatePointListGlobalCS(step)
                .mapLeft { it.toIllegalStateException() }
                .getOrElse { throw it }

        return LinearRing3D.ofWithDuplicatesRemoval(
            leftRoadMarkingBoundary,
            rightRoadMarkingBoundary,
            geometricalTolerance,
        )
            .getOrElse { throw IllegalStateException(it.message) }
            .let { CompositeSurface3D(it) }
    }

    /**
     * Returns all road markings of a lane with [laneIdentifier].
     *
     * @param laneIdentifier lane identifier for which the road markings shall be returned
     * @param step discretization step size
     */
    private fun getLeftRightRoadMarkings(
        laneIdentifier: LaneIdentifier,
        step: Double,
    ): List<Either<Exception, Pair<RoadMarking, AbstractGeometry3D>>> {
        require(laneIdentifier.isLeft() || laneIdentifier.isRight()) { "Identifier of lane must represent a left or a right lane." }

        return getLane(laneIdentifier)
            .getOrElse { throw it }
            .roadMarkings
            .map { currentRoadMarking ->
                val geometry: AbstractGeometry3D =
                    getRoadMarkingGeometry(laneIdentifier, currentRoadMarking, step)
                        .getOrElse { return@map it.left() }
                Either.Right(currentRoadMarking to geometry)
            }
    }

    /**
     * Returns the geometry of a [roadMarking] which is attached to a lane with [laneIdentifier].
     *
     * @param laneIdentifier identifier of the lane to which the [roadMarking] belongs
     * @param roadMarking road marking for which the geometry shall be returned
     * @return either a [AbstractCurve3D], if the road marking has zero width, or a [AbstractSurface3D], if the width
     * is not zero
     */
    private fun getRoadMarkingGeometry(
        laneIdentifier: LaneIdentifier,
        roadMarking: RoadMarking,
        step: Double,
    ): Either<Exception, AbstractGeometry3D> {
        if (roadMarking.width.domain.length < geometricalTolerance) {
            return Either.Left(
                IllegalStateException(
                    "${laneIdentifier.toIdentifierText()}: Road marking's length is zero (or below tolerance threshold) and " +
                        "thus no surface can be constructed.",
                ),
            )
        }

        if (roadMarking.width.value < geometricalTolerance) {
            return getCurveOnLane(laneIdentifier, 1.0, roadMarking.width)
        }

        val leftOffsetFunction = roadMarking.width timesValue 0.5
        val leftRoadMarkBoundary =
            getCurveOnLane(laneIdentifier, 1.0, leftOffsetFunction)
                .getOrElse { throw it }
                .calculatePointListGlobalCS(step)
                .mapLeft { it.toIllegalStateException() }
                .getOrElse { throw it }
        val rightOffsetFunction = roadMarking.width timesValue -0.5
        val rightRoadMarkBoundary =
            getCurveOnLane(laneIdentifier, 1.0, rightOffsetFunction)
                .getOrElse { throw it }
                .calculatePointListGlobalCS(step)
                .mapLeft { it.toIllegalStateException() }
                .getOrElse { throw it }

        return LinearRing3D.ofWithDuplicatesRemoval(leftRoadMarkBoundary, rightRoadMarkBoundary, geometricalTolerance)
            .getOrElse { return IllegalStateException(it.message).left() }
            .let { CompositeSurface3D(it) }
            .let { Either.Right(it) }
    }

    /** Returns the curve position domains of each lane section. */
    private fun getLaneSectionCurvePositionDomains(): List<Range<Double>> {
        val laneSectionDomains =
            laneSections
                .map { it.curvePositionStart.curvePosition }
                .zipWithNext()
                .map { Range.closed(it.first, it.second) }

        val lastLaneSectionDomain =
            Range.closedX(
                laneSections.last().curvePositionStart.curvePosition,
                curvePositionDomain.upperEndpointOrNull()!!,
                curvePositionDomain.upperBoundType(),
            )

        return laneSectionDomains + lastLaneSectionDomain
    }
}
