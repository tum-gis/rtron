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

package io.rtron.transformer.converter.roadspaces2citygml.module

import arrow.core.Option
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.solid.AbstractSolid3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.model.roadspaces.common.LateralFillerSurface
import io.rtron.model.roadspaces.common.LongitudinalFillerSurface
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.LaneType
import io.rtron.model.roadspaces.roadspace.road.RoadMarking
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1MultiSurface
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2MultiSurfaceFromSolidCutoutOrSurface
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2MultiSurfaceOrLod0Geometry
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveRoadMarkingGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier
import io.rtron.transformer.issues.roadspaces.of
import org.citygml4j.core.model.core.AbstractSpaceBoundaryProperty
import org.citygml4j.core.model.core.AbstractThematicSurface
import org.citygml4j.core.model.core.SpaceType
import org.citygml4j.core.model.transportation.AbstractTransportationSpace
import org.citygml4j.core.model.transportation.AuxiliaryTrafficArea
import org.citygml4j.core.model.transportation.AuxiliaryTrafficSpace
import org.citygml4j.core.model.transportation.AuxiliaryTrafficSpaceProperty
import org.citygml4j.core.model.transportation.GranularityValue
import org.citygml4j.core.model.transportation.Intersection
import org.citygml4j.core.model.transportation.Marking
import org.citygml4j.core.model.transportation.MarkingProperty
import org.citygml4j.core.model.transportation.Road
import org.citygml4j.core.model.transportation.Section
import org.citygml4j.core.model.transportation.TrafficArea
import org.citygml4j.core.model.transportation.TrafficDirectionValue
import org.citygml4j.core.model.transportation.TrafficSpace
import org.citygml4j.core.model.transportation.TrafficSpaceProperty

enum class TransportationGranularityValue { LANE, WAY }

fun TransportationGranularityValue.toGmlGranularityValue(): GranularityValue =
    when (this) {
        TransportationGranularityValue.LANE -> GranularityValue.LANE
        TransportationGranularityValue.WAY -> GranularityValue.WAY
    }

/**
 * Builder for city objects of the CityGML Transportation module.
 */
class TransportationModuleBuilder(
    val parameters: Roadspaces2CitygmlParameters,
) {
    // Properties and Initializers
    private val relationAdder = RelationAdder(parameters)
    private val attributesAdder = AttributesAdder(parameters)

    // Methods
    fun createRoad() = Road()

    fun createSection() = Section()

    fun createIntersection() = Intersection()

    fun createMarking() = Marking()

    /**
     * Transforms a [lane] with a [surface] and [centerLine] representation and its [longitudinalFillerSurfaces] to a
     * CityGML [TrafficSpace] and adds it to the [dstTransportationSpace].
     */
    fun addTrafficSpaceFeature(
        lane: Lane,
        surface: AbstractSurface3D,
        extrudedSurface: Option<AbstractSolid3D>,
        centerLine: AbstractCurve3D,
        lateralFillerSurface: Option<LateralFillerSurface>,
        longitudinalFillerSurfaces: List<LongitudinalFillerSurface>,
        relatedObjects: List<RoadspaceObject>,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        val trafficSpaceFeature = createTrafficSpaceFeature(TransportationGranularityValue.LANE)
        // semantics
        IdentifierAdder.addIdentifier(
            lane.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix),
            trafficSpaceFeature,
        )
        trafficSpaceFeature.usages = CodeAdder.mapToTrafficAreaUsageCodes(lane.type).map { it.code }
        trafficSpaceFeature.functions = CodeAdder.mapToTrafficAreaFunctionCodes(lane.type).map { it.code }
        attributesAdder.addAttributes(lane, trafficSpaceFeature)
        relatedObjects.forEach { relationAdder.addRelatedToRelation(it, trafficSpaceFeature) }
        // TODO: consider left-hand traffic (LHT)
        trafficSpaceFeature.trafficDirection =
            when {
                lane.type == LaneType.BIDIRECTIONAL -> TrafficDirectionValue.BOTH
                lane.id.isForward() -> TrafficDirectionValue.FORWARDS
                else -> TrafficDirectionValue.BACKWARDS
            }

        // geometry
        val centerLineGeometryTransformer = GeometryTransformer(parameters).also { centerLine.accept(it) }
        trafficSpaceFeature.populateLod2Geometry(centerLineGeometryTransformer)

        extrudedSurface.onSome { currentExtrudedSurface ->
            val extrudedSurfaceGeometryTransformer = GeometryTransformer(parameters).also { currentExtrudedSurface.accept(it) }
            trafficSpaceFeature.populateLod2Geometry(extrudedSurfaceGeometryTransformer)
        }

        // traffic area feature
        val trafficAreaFeature = createTrafficAreaFeature(lane.id, surface).handleIssueList { issueList += it }
        IdentifierAdder.addIdentifier(
            lane.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix),
            "Lane",
            trafficAreaFeature,
        )
        trafficAreaFeature.usages = CodeAdder.mapToTrafficAreaUsageCodes(lane.type).map { it.code }
        trafficAreaFeature.functions = CodeAdder.mapToTrafficAreaFunctionCodes(lane.type).map { it.code }
        lane.laneMaterial
            .flatMap { CodeAdder.mapToTrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode(it) }
            .onSome { trafficAreaFeature.surfaceMaterial = it.code }
        attributesAdder.addAttributes(lane, trafficAreaFeature)
        trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(trafficAreaFeature))

        // filler surface features
        lateralFillerSurface.onSome { fillerSurface ->
            val fillerTrafficArea =
                createTrafficAreaFeature(
                    fillerSurface.id,
                    fillerSurface.surface,
                ).handleIssueList { issueList += it }

            IdentifierAdder.addIdentifier(
                fillerSurface.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(
                    parameters.gmlIdPrefix,
                ),
                "LateralFillerSurface",
                fillerTrafficArea,
            )
            attributesAdder.addAttributes(fillerSurface, fillerTrafficArea)
            trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerTrafficArea))
        }
        longitudinalFillerSurfaces.forEach { fillerSurface ->
            val fillerTrafficArea =
                createTrafficAreaFeature(lane.id, fillerSurface.surface).handleIssueList { issueList += it }

            IdentifierAdder.addIdentifier(
                fillerSurface.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(
                    parameters.gmlIdPrefix,
                ),
                "LongitudinalFillerSurface",
                fillerTrafficArea,
            )
            attributesAdder.addAttributes(fillerSurface, fillerTrafficArea)
            trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerTrafficArea))
        }

        // populate transportation space
        val trafficSpaceProperty = TrafficSpaceProperty(trafficSpaceFeature)
        dstTransportationSpace.trafficSpaces.add(trafficSpaceProperty)

        return issueList
    }

    /**
     * Transforms a [lane] with a [surface] and [centerLine] representation and its [longitudinalFillerSurfaces] to a
     * CityGML [AuxiliaryTrafficSpace] and adds it to the [dstTransportationSpace].
     */
    fun addAuxiliaryTrafficSpaceFeature(
        lane: Lane,
        surface: AbstractSurface3D,
        extrudedSurface: Option<AbstractSolid3D>,
        centerLine: AbstractCurve3D,
        lateralFillerSurface: Option<LateralFillerSurface>,
        longitudinalFillerSurfaces: List<LongitudinalFillerSurface>,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        val auxiliaryTrafficSpaceFeature = createAuxiliaryTrafficSpaceFeature(TransportationGranularityValue.LANE)
        // semantics
        IdentifierAdder.addIdentifier(
            lane.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix),
            auxiliaryTrafficSpaceFeature,
        )
        auxiliaryTrafficSpaceFeature.functions =
            CodeAdder.mapToAuxiliaryTrafficAreaFunctionCodes(lane.type).map { it.code }
        attributesAdder.addAttributes(lane, auxiliaryTrafficSpaceFeature)
        // geometry
        val centerLineGeometryTransformer = GeometryTransformer(parameters).also { centerLine.accept(it) }
        auxiliaryTrafficSpaceFeature.populateLod2Geometry(centerLineGeometryTransformer)

        extrudedSurface.onSome { currentExtrudedSurface ->
            val extrudedSurfaceGeometryTransformer = GeometryTransformer(parameters).also { currentExtrudedSurface.accept(it) }
            auxiliaryTrafficSpaceFeature.populateLod2Geometry(extrudedSurfaceGeometryTransformer)
        }

        // auxiliary traffic area feature
        val auxiliaryTrafficAreaFeature =
            createAuxiliaryTrafficAreaFeature(lane.id, surface)
                .handleIssueList { issueList += it }
        IdentifierAdder.addIdentifier(
            lane.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix),
            "Lane",
            auxiliaryTrafficAreaFeature,
        )
        auxiliaryTrafficAreaFeature.functions =
            CodeAdder.mapToAuxiliaryTrafficAreaFunctionCodes(lane.type).map { it.code }
        lane.laneMaterial
            .flatMap { CodeAdder.mapToTrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode(it) }
            .onSome { auxiliaryTrafficAreaFeature.surfaceMaterial = it.code }
        attributesAdder.addAttributes(lane, auxiliaryTrafficAreaFeature)
        auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(auxiliaryTrafficAreaFeature))

        // filler surface features
        lateralFillerSurface.onSome { fillerSurface ->
            val fillerAuxiliaryTrafficArea =
                createAuxiliaryTrafficAreaFeature(
                    fillerSurface.id,
                    fillerSurface.surface,
                ).handleIssueList { issueList += it }

            IdentifierAdder.addIdentifier(
                fillerSurface.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(
                    parameters.gmlIdPrefix,
                ),
                "LateralFillerSurface",
                fillerAuxiliaryTrafficArea,
            )
            attributesAdder.addAttributes(fillerSurface, fillerAuxiliaryTrafficArea)
            auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerAuxiliaryTrafficArea))
        }
        longitudinalFillerSurfaces.forEach { fillerSurface ->
            val fillerAuxiliaryTrafficArea =
                createAuxiliaryTrafficAreaFeature(lane.id, fillerSurface.surface)
                    .handleIssueList { issueList += it }

            IdentifierAdder.addIdentifier(
                fillerSurface.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(
                    parameters.gmlIdPrefix,
                ),
                "LongitudinalFillerSurface",
                fillerAuxiliaryTrafficArea,
            )
            attributesAdder.addAttributes(fillerSurface, fillerAuxiliaryTrafficArea)
            auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerAuxiliaryTrafficArea))
        }

        // populate transportation space
        val auxiliaryTrafficSpaceProperty = AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpaceFeature)
        dstTransportationSpace.auxiliaryTrafficSpaces.add(auxiliaryTrafficSpaceProperty)

        return issueList
    }

    fun addTrafficSpaceFeature(
        roadspaceObject: RoadspaceObject,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        val trafficSpaceFeature = createTrafficSpaceFeature(TransportationGranularityValue.LANE)
        IdentifierAdder.addIdentifier(
            roadspaceObject.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(
                parameters.gmlIdPrefix,
            ),
            trafficSpaceFeature,
        )
        trafficSpaceFeature.usages = CodeAdder.mapToTrafficAreaUsageCodes(roadspaceObject.type).map { it.code }
        trafficSpaceFeature.functions = CodeAdder.mapToTrafficAreaFunctionCodes(roadspaceObject.type).map { it.code }
        attributesAdder.addAttributes(roadspaceObject, trafficSpaceFeature)

        // surface representation
        roadspaceObject.complexGeometry.onSome { currentComplexGeometry ->
            val trafficAreaFeature = TrafficArea()
            // semantics
            IdentifierAdder.addIdentifier(
                roadspaceObject.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(
                    parameters.gmlIdPrefix,
                ),
                trafficAreaFeature,
            )
            trafficAreaFeature.usages = CodeAdder.mapToTrafficAreaUsageCodes(roadspaceObject.type).map { it.code }
            trafficAreaFeature.functions = CodeAdder.mapToTrafficAreaFunctionCodes(roadspaceObject.type).map { it.code }
            attributesAdder.addAttributes(roadspaceObject, trafficAreaFeature)

            // geometry
            val geometryTransformer = GeometryTransformer.of(currentComplexGeometry, parameters)
            val solidFaceSelection = listOf(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
            trafficAreaFeature
                .populateLod2MultiSurfaceFromSolidCutoutOrSurface(geometryTransformer, solidFaceSelection)
                .onLeft {
                    issueList +=
                        DefaultIssue.of(
                            "NoSuitableGeometryForTrafficAreaLod2",
                            it.message,
                            roadspaceObject.id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                }

            trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(trafficAreaFeature))
        }

        roadspaceObject.extrudedTopSurfaceGeometry.onSome { currentExtrudedTopSurfaceGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentExtrudedTopSurfaceGeometry, parameters)
            trafficSpaceFeature
                .populateLod2Geometry(geometryTransformer)
                .onLeft {
                    issueList +=
                        DefaultIssue.of(
                            "NoSuitableGeometryForTrafficSpaceLod2",
                            it.message,
                            roadspaceObject.id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                }
        }

        // populate transportation space
        val trafficSpaceProperty = TrafficSpaceProperty(trafficSpaceFeature)
        dstTransportationSpace.trafficSpaces.add(trafficSpaceProperty)

        return issueList
    }

    fun addAuxiliaryTrafficSpaceFeature(
        roadspaceObject: RoadspaceObject,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()

        val auxiliaryTrafficSpaceFeature = createAuxiliaryTrafficSpaceFeature(TransportationGranularityValue.LANE)
        IdentifierAdder.addIdentifier(
            roadspaceObject.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(
                parameters.gmlIdPrefix,
            ),
            auxiliaryTrafficSpaceFeature,
        )
        auxiliaryTrafficSpaceFeature.functions =
            CodeAdder.mapToAuxiliaryTrafficAreaFunctionCodes(roadspaceObject.type).map { it.code }

        // surface representation
        roadspaceObject.complexGeometry.onSome { currentComplexGeometry ->
            val auxiliaryTrafficAreaFeature = AuxiliaryTrafficArea()
            // semantics
            IdentifierAdder.addIdentifier(
                roadspaceObject.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(
                    parameters.gmlIdPrefix,
                ),
                auxiliaryTrafficAreaFeature,
            )
            auxiliaryTrafficAreaFeature.functions =
                CodeAdder.mapToAuxiliaryTrafficAreaFunctionCodes(roadspaceObject.type).map { it.code }
            attributesAdder.addAttributes(roadspaceObject, auxiliaryTrafficAreaFeature)

            // geometry
            val geometryTransformer = GeometryTransformer.of(currentComplexGeometry, parameters)
            val solidFaceSelection = listOf(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
            auxiliaryTrafficAreaFeature
                .populateLod2MultiSurfaceFromSolidCutoutOrSurface(
                    geometryTransformer,
                    solidFaceSelection,
                ).onLeft {
                    issueList +=
                        DefaultIssue.of(
                            "NoSuitableGeometryForAuxiliaryTrafficAreaLod2",
                            it.message,
                            roadspaceObject.id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                }

            auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(auxiliaryTrafficAreaFeature))
        }

        roadspaceObject.extrudedTopSurfaceGeometry.onSome { currentExtrudedTopSurfaceGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentExtrudedTopSurfaceGeometry, parameters)
            auxiliaryTrafficSpaceFeature
                .populateLod2Geometry(geometryTransformer)
                .onLeft {
                    issueList +=
                        DefaultIssue.of(
                            "NoSuitableGeometryForAuxiliaryTrafficSpaceLod2",
                            it.message,
                            roadspaceObject.id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                }
        }

        // populate transportation space
        val auxiliaryTrafficSpaceProperty = AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpaceFeature)
        dstTransportationSpace.auxiliaryTrafficSpaces.add(auxiliaryTrafficSpaceProperty)

        return issueList
    }

    fun addMarkingFeature(
        id: LaneIdentifier,
        roadMarkingIndex: Int,
        roadMarking: RoadMarking,
        geometry: AbstractGeometry3D,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()
        val markingFeature = if (parameters.mappingBackwardsCompatibility) AuxiliaryTrafficArea() else createMarking()

        // geometry
        val geometryTransformer = GeometryTransformer(parameters).also { geometry.accept(it) }
        markingFeature
            .populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer)
            .onLeft {
                issueList +=
                    DefaultIssue.of(
                        "NoSuitableGeometryForMarkingLod2",
                        it.message,
                        id,
                        Severity.WARNING,
                        wasFixed = true,
                    )
            }

        // semantics
        IdentifierAdder.addIdentifier(
            id.deriveRoadMarkingGmlIdentifier(parameters.gmlIdPrefix, roadMarkingIndex),
            "RoadMarking",
            markingFeature,
        )
        attributesAdder.addAttributes(id, roadMarking, markingFeature)

        // populate transportation space
        addMarkingFeature(markingFeature, dstTransportationSpace)
        return issueList
    }

    fun addMarkingFeature(
        roadspaceObject: RoadspaceObject,
        dstTransportationSpace: AbstractTransportationSpace,
    ): DefaultIssueList {
        val issueList = DefaultIssueList()
        val markingFeature = if (parameters.mappingBackwardsCompatibility) AuxiliaryTrafficArea() else createMarking()

        // geometry
        roadspaceObject.boundingBoxGeometry.onSome { currentBoundingBoxGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentBoundingBoxGeometry, parameters)
            markingFeature
                .populateLod1MultiSurface(geometryTransformer)
                .onLeft {
                    issueList +=
                        DefaultIssue.of(
                            "NoSuitableGeometryForMarkingLod1",
                            it.message,
                            roadspaceObject.id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                }
        }
        roadspaceObject.complexGeometry.onSome { currentComplexGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentComplexGeometry, parameters)
            markingFeature
                .populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer)
                .onLeft {
                    issueList +=
                        DefaultIssue.of(
                            "NoSuitableGeometryForMarkingLod2",
                            it.message,
                            roadspaceObject.id,
                            Severity.WARNING,
                            wasFixed = true,
                        )
                }
        }

        // semantics
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix), markingFeature)
        attributesAdder.addAttributes(roadspaceObject, markingFeature)

        // populate transportation space
        addMarkingFeature(markingFeature, dstTransportationSpace)
        return issueList
    }

    private fun createTrafficSpaceFeature(granularity: TransportationGranularityValue): TrafficSpace {
        val trafficSpaceFeature = TrafficSpace()
        trafficSpaceFeature.granularity = granularity.toGmlGranularityValue()
        trafficSpaceFeature.spaceType = SpaceType.OPEN
        return trafficSpaceFeature
    }

    private fun createAuxiliaryTrafficSpaceFeature(granularity: TransportationGranularityValue): AuxiliaryTrafficSpace {
        val auxiliaryTrafficSpaceFeature = AuxiliaryTrafficSpace()
        auxiliaryTrafficSpaceFeature.granularity = granularity.toGmlGranularityValue()
        auxiliaryTrafficSpaceFeature.spaceType = SpaceType.OPEN
        return auxiliaryTrafficSpaceFeature
    }

    private fun createTrafficAreaFeature(
        id: AbstractRoadspacesIdentifier,
        abstractGeometry: AbstractGeometry3D,
    ): ContextIssueList<TrafficArea> {
        val issueList = DefaultIssueList()
        val trafficAreaFeature = TrafficArea()

        val geometryTransformer =
            GeometryTransformer(parameters)
                .also { abstractGeometry.accept(it) }
        val solidFaceSelection = listOf(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
        trafficAreaFeature
            .populateLod2MultiSurfaceFromSolidCutoutOrSurface(geometryTransformer, solidFaceSelection)
            .onLeft {
                issueList +=
                    DefaultIssue.of(
                        "NoSuitableGeometryForTrafficAreaLod2",
                        it.message,
                        id,
                        Severity.WARNING,
                        wasFixed = true,
                    )
            }

        return ContextIssueList(trafficAreaFeature, issueList)
    }

    private fun createAuxiliaryTrafficAreaFeature(
        id: AbstractRoadspacesIdentifier,
        abstractGeometry: AbstractGeometry3D,
    ): ContextIssueList<AuxiliaryTrafficArea> {
        val issueList = DefaultIssueList()
        val auxiliaryTrafficAreaFeature = AuxiliaryTrafficArea()

        val geometryTransformer =
            GeometryTransformer(parameters)
                .also { abstractGeometry.accept(it) }

        val solidFaceSelection = listOf(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
        auxiliaryTrafficAreaFeature
            .populateLod2MultiSurfaceFromSolidCutoutOrSurface(
                geometryTransformer,
                solidFaceSelection,
            ).onLeft {
                issueList +=
                    DefaultIssue.of(
                        "NoSuitableGeometryForAuxiliaryTrafficAreaLod2",
                        it.message,
                        id,
                        Severity.WARNING,
                        wasFixed = true,
                    )
            }

        return ContextIssueList(auxiliaryTrafficAreaFeature, issueList)
    }

    /**
     * Add the [markingFeature] to the [dstTransportationSpace] depending on its type.
     */
    private fun addMarkingFeature(
        markingFeature: AbstractThematicSurface,
        dstTransportationSpace: AbstractTransportationSpace,
    ) {
        when (markingFeature) {
            is Marking -> {
                val markingProperty = MarkingProperty(markingFeature)
                dstTransportationSpace.markings.add(markingProperty)
            }

            is AuxiliaryTrafficArea -> {
                // for backwards compatibility
                val auxiliaryTrafficSpace = AuxiliaryTrafficSpace()
                auxiliaryTrafficSpace.addBoundary(AbstractSpaceBoundaryProperty(markingFeature))
                dstTransportationSpace.auxiliaryTrafficSpaces.add(AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpace))
            }

            else -> {
                throw IllegalStateException("MarkingFeature is of unsuitable type for adding to the transportation space.")
            }
        }
    }
}
