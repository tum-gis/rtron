/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
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
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.model.roadspaces.common.LateralFillerSurface
import io.rtron.model.roadspaces.common.LongitudinalFillerSurface
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.RoadMarking
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2MultiSurfaceFromSolidCutoutOrSurface
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2MultiSurfaceOrLod0Geometry
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveRoadMarkingGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier
import io.rtron.transformer.messages.roadspaces.of
import org.citygml4j.core.model.core.AbstractSpaceBoundaryProperty
import org.citygml4j.core.model.core.AbstractThematicSurface
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

fun TransportationGranularityValue.toGmlGranularityValue(): GranularityValue = when (this) {
    TransportationGranularityValue.LANE -> GranularityValue.LANE
    TransportationGranularityValue.WAY -> GranularityValue.WAY
}

/**
 * Builder for city objects of the CityGML Transportation module.
 */
class TransportationModuleBuilder(
    val parameters: Roadspaces2CitygmlParameters
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
    fun addTrafficSpaceFeature(lane: Lane, surface: AbstractSurface3D, centerLine: AbstractCurve3D, lateralFillerSurface: Option<LateralFillerSurface>, longitudinalFillerSurfaces: List<LongitudinalFillerSurface>, relatedObjects: List<RoadspaceObject>, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()

        val trafficSpaceFeature = createTrafficSpaceFeature(TransportationGranularityValue.LANE)
        IdentifierAdder.addIdentifier(lane.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix), trafficSpaceFeature)
        attributesAdder.addAttributes(lane, trafficSpaceFeature)
        relatedObjects.forEach { relationAdder.addRelatedToRelation(it, trafficSpaceFeature) }
        // TODO: consider left-hand traffic (LHT)
        if (lane.id.isRight() || lane.id.isCenter()) {
            trafficSpaceFeature.trafficDirection = TrafficDirectionValue.FORWARDS
        } else {
            trafficSpaceFeature.trafficDirection = TrafficDirectionValue.BACKWARDS
        }

        // line representation of lane
        val centerLineGeometryTransformer = GeometryTransformer(parameters).also { centerLine.accept(it) }
        trafficSpaceFeature.populateLod2Geometry(centerLineGeometryTransformer)

        // surface representation of lane
        val trafficArea = createTrafficAreaFeature(lane.id, surface).handleMessageList { messageList += it }
        trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(trafficArea))

        IdentifierAdder.addIdentifier(lane.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix), "Lane", trafficArea)
        trafficArea.usages = CodeAdder.mapToTrafficAreaUsageCodes(lane.type).map { it.code }
        trafficArea.functions = CodeAdder.mapToTrafficAreaFunctionCodes(lane.type).map { it.code }
        lane.laneMaterial.flatMap { CodeAdder.mapToTrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode(it) }
            .tap { trafficArea.surfaceMaterial = it.code }
        attributesAdder.addAttributes(lane, trafficArea)

        // filler surfaces
        lateralFillerSurface.tap { fillerSurface ->
            val fillerTrafficArea = createTrafficAreaFeature(fillerSurface.id, fillerSurface.surface).handleMessageList { messageList += it }

            IdentifierAdder.addIdentifier(fillerSurface.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix), "LateralFillerSurface", fillerTrafficArea)
            attributesAdder.addAttributes(fillerSurface, fillerTrafficArea)
            trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerTrafficArea))
        }
        longitudinalFillerSurfaces.forEach { fillerSurface ->
            val fillerTrafficArea = createTrafficAreaFeature(lane.id, fillerSurface.surface).handleMessageList { messageList += it }

            IdentifierAdder.addIdentifier(fillerSurface.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix), "LongitudinalFillerSurface", fillerTrafficArea)
            attributesAdder.addAttributes(fillerSurface, fillerTrafficArea)
            trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerTrafficArea))
        }

        // populate transportation space
        val trafficSpaceProperty = TrafficSpaceProperty(trafficSpaceFeature)
        dstTransportationSpace.trafficSpaces.add(trafficSpaceProperty)

        return messageList
    }

    /**
     * Transforms a [lane] with a [surface] and [centerLine] representation and its [longitudinalFillerSurfaces] to a
     * CityGML [AuxiliaryTrafficSpace] and adds it to the [dstTransportationSpace].
     */
    fun addAuxiliaryTrafficSpaceFeature(
        lane: Lane,
        surface: AbstractSurface3D,
        centerLine: AbstractCurve3D,
        lateralFillerSurface: Option<LateralFillerSurface>,
        longitudinalFillerSurfaces: List<LongitudinalFillerSurface>,
        dstTransportationSpace: AbstractTransportationSpace
    ): DefaultMessageList {
        val messageList = DefaultMessageList()
        val auxiliaryTrafficSpaceFeature = createAuxiliaryTrafficSpaceFeature(TransportationGranularityValue.LANE)
        IdentifierAdder.addIdentifier(lane.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix), auxiliaryTrafficSpaceFeature)
        attributesAdder.addAttributes(lane, auxiliaryTrafficSpaceFeature)

        // line representation
        val centerLineGeometryTransformer = GeometryTransformer(parameters).also { centerLine.accept(it) }
        auxiliaryTrafficSpaceFeature.populateLod2Geometry(centerLineGeometryTransformer)

        // surface representation
        val auxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(lane.id, surface)
            .handleMessageList { messageList += it }
        auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(auxiliaryTrafficArea))

        IdentifierAdder.addIdentifier(lane.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix), "Lane", auxiliaryTrafficArea)
        auxiliaryTrafficArea.functions = CodeAdder.mapToAuxiliaryTrafficAreaFunctionCodes(lane.type).map { it.code }
        lane.laneMaterial.flatMap { CodeAdder.mapToTrafficAreaAndAuxiliaryTrafficAreaSurfaceMaterialCode(it) }
            .tap { auxiliaryTrafficArea.surfaceMaterial = it.code }
        attributesAdder.addAttributes(lane, auxiliaryTrafficArea)

        // filler surfaces
        lateralFillerSurface.tap { fillerSurface ->
            val fillerAuxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(fillerSurface.id, fillerSurface.surface).handleMessageList { messageList += it }

            IdentifierAdder.addIdentifier(fillerSurface.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix), "LateralFillerSurface", fillerAuxiliaryTrafficArea)
            attributesAdder.addAttributes(fillerSurface, fillerAuxiliaryTrafficArea)
            auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerAuxiliaryTrafficArea))
        }
        longitudinalFillerSurfaces.forEach { fillerSurface ->
            val fillerAuxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(lane.id, fillerSurface.surface)
                .handleMessageList { messageList += it }

            IdentifierAdder.addIdentifier(fillerSurface.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix), "LongitudinalFillerSurface", fillerAuxiliaryTrafficArea)
            attributesAdder.addAttributes(fillerSurface, fillerAuxiliaryTrafficArea)
            auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerAuxiliaryTrafficArea))
        }

        // populate transportation space
        val auxiliaryTrafficSpaceProperty = AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpaceFeature)
        dstTransportationSpace.auxiliaryTrafficSpaces.add(auxiliaryTrafficSpaceProperty)
        return messageList
    }

    fun addTrafficSpaceFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()
        val trafficSpaceFeature = createTrafficSpaceFeature(TransportationGranularityValue.LANE)
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix), trafficSpaceFeature)

        // surface representation
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, parameters)
        val trafficArea = createTrafficAreaFeature(roadspaceObject.id, geometryTransformer)
            .handleMessageList { messageList += it }
        trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(trafficArea))

        // semantics
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix), trafficArea)
        attributesAdder.addAttributes(roadspaceObject, trafficArea)

        // populate transportation space
        val trafficSpaceProperty = TrafficSpaceProperty(trafficSpaceFeature)
        dstTransportationSpace.trafficSpaces.add(trafficSpaceProperty)
        return messageList
    }

    fun addAuxiliaryTrafficSpaceFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()
        val auxiliaryTrafficSpaceFeature = createAuxiliaryTrafficSpaceFeature(TransportationGranularityValue.LANE)
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(parameters.gmlIdPrefix), auxiliaryTrafficSpaceFeature)

        // surface representation
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, parameters)
        val auxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(roadspaceObject.id, geometryTransformer)
            .handleMessageList { messageList += it }
        auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(auxiliaryTrafficArea))

        // semantics
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(parameters.gmlIdPrefix), auxiliaryTrafficArea)
        attributesAdder.addAttributes(roadspaceObject, auxiliaryTrafficArea)

        // populate transportation space
        val auxiliaryTrafficSpaceProperty = AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpaceFeature)
        dstTransportationSpace.auxiliaryTrafficSpaces.add(auxiliaryTrafficSpaceProperty)
        return messageList
    }

    fun addMarkingFeature(id: LaneIdentifier, roadMarkingIndex: Int, roadMarking: RoadMarking, geometry: AbstractGeometry3D, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()
        val markingFeature = if (parameters.mappingBackwardsCompatibility) AuxiliaryTrafficArea() else createMarking()

        // geometry
        val geometryTransformer = GeometryTransformer(parameters).also { geometry.accept(it) }
        markingFeature.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer)
            .onLeft { messageList += DefaultMessage.of("", it.message, id, Severity.WARNING, wasFixed = true) }

        // semantics
        IdentifierAdder.addIdentifier(id.deriveRoadMarkingGmlIdentifier(parameters.gmlIdPrefix, roadMarkingIndex), "RoadMarking", markingFeature)
        attributesAdder.addAttributes(id, roadMarking, markingFeature)

        // populate transportation space
        addMarkingFeature(markingFeature, dstTransportationSpace)
        return messageList
    }

    fun addMarkingFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): DefaultMessageList {
        val messageList = DefaultMessageList()
        val markingFeature = if (parameters.mappingBackwardsCompatibility) AuxiliaryTrafficArea() else createMarking()

        // geometry
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, parameters)
        markingFeature.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer)
            .onLeft { messageList += DefaultMessage.of("", it.message, roadspaceObject.id, Severity.WARNING, wasFixed = true) }

        // semantics
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix), markingFeature)
        attributesAdder.addAttributes(roadspaceObject, markingFeature)

        // populate transportation space
        addMarkingFeature(markingFeature, dstTransportationSpace)
        return messageList
    }

    private fun createTrafficSpaceFeature(granularity: TransportationGranularityValue): TrafficSpace {
        val trafficSpaceFeature = TrafficSpace()
        trafficSpaceFeature.granularity = granularity.toGmlGranularityValue()
        return trafficSpaceFeature
    }

    private fun createAuxiliaryTrafficSpaceFeature(granularity: TransportationGranularityValue): AuxiliaryTrafficSpace {
        val auxiliaryTrafficSpaceFeature = AuxiliaryTrafficSpace()
        auxiliaryTrafficSpaceFeature.granularity = granularity.toGmlGranularityValue()
        return auxiliaryTrafficSpaceFeature
    }

    private fun createTrafficAreaFeature(id: AbstractRoadspacesIdentifier, abstractGeometry: AbstractGeometry3D): ContextMessageList<TrafficArea> {
        val geometryTransformer = GeometryTransformer(parameters)
            .also { abstractGeometry.accept(it) }
        return createTrafficAreaFeature(id, geometryTransformer)
    }

    private fun createTrafficAreaFeature(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextMessageList<TrafficArea> {
        val messageList = DefaultMessageList()
        val trafficAreaFeature = TrafficArea()

        val solidFaceSelection = listOf(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
        trafficAreaFeature.populateLod2MultiSurfaceFromSolidCutoutOrSurface(geometryTransformer, solidFaceSelection)
            .onLeft { messageList += DefaultMessage.of("", it.message, id, Severity.WARNING, wasFixed = true) }

        return ContextMessageList(trafficAreaFeature, messageList)
    }

    private fun createAuxiliaryTrafficAreaFeature(id: AbstractRoadspacesIdentifier, abstractGeometry: AbstractGeometry3D): ContextMessageList<AuxiliaryTrafficArea> {
        val geometryTransformer = GeometryTransformer(parameters)
            .also { abstractGeometry.accept(it) }
        return createAuxiliaryTrafficAreaFeature(id, geometryTransformer)
    }

    private fun createAuxiliaryTrafficAreaFeature(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextMessageList<AuxiliaryTrafficArea> {
        val messageList = DefaultMessageList()
        val auxiliaryTrafficAreaFeature = AuxiliaryTrafficArea()

        val solidFaceSelection = listOf(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
        auxiliaryTrafficAreaFeature.populateLod2MultiSurfaceFromSolidCutoutOrSurface(geometryTransformer, solidFaceSelection)
            .onLeft { messageList += DefaultMessage.of("", it.message, id, Severity.WARNING, wasFixed = true) }

        return ContextMessageList(auxiliaryTrafficAreaFeature, messageList)
    }

    /**
     * Add the [markingFeature] to the [dstTransportationSpace] depending on its type.
     */
    private fun addMarkingFeature(markingFeature: AbstractThematicSurface, dstTransportationSpace: AbstractTransportationSpace) {
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
            else -> { throw IllegalStateException("MarkingFeature is of unsuitable type for adding to the transportation space.") }
        }
    }
}
