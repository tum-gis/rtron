/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.model.roadspaces.common.FillerSurface
import io.rtron.model.roadspaces.common.LateralFillerSurface
import io.rtron.model.roadspaces.common.LongitudinalFillerSurfaceBetweenRoads
import io.rtron.model.roadspaces.common.LongitudinalFillerSurfaceWithinRoad
import io.rtron.model.roadspaces.identifier.AbstractRoadspacesIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.RoadMarking
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2MultiSurfaceFromSolidCutoutOrSurface
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2MultiSurfaceOrLod0Geometry
import io.rtron.transformer.report.of
import org.citygml4j.model.core.AbstractSpaceBoundaryProperty
import org.citygml4j.model.transportation.AbstractTransportationSpace
import org.citygml4j.model.transportation.AuxiliaryTrafficArea
import org.citygml4j.model.transportation.AuxiliaryTrafficSpace
import org.citygml4j.model.transportation.AuxiliaryTrafficSpaceProperty
import org.citygml4j.model.transportation.GranularityValue
import org.citygml4j.model.transportation.Intersection
import org.citygml4j.model.transportation.Marking
import org.citygml4j.model.transportation.MarkingProperty
import org.citygml4j.model.transportation.Road
import org.citygml4j.model.transportation.Section
import org.citygml4j.model.transportation.TrafficArea
import org.citygml4j.model.transportation.TrafficSpace
import org.citygml4j.model.transportation.TrafficSpaceProperty

enum class TransportationGranularityValue { LANE, WAY }

fun TransportationGranularityValue.toGmlGranularityValue(): GranularityValue = when (this) {
    TransportationGranularityValue.LANE -> GranularityValue.LANE
    TransportationGranularityValue.WAY -> GranularityValue.WAY
}

fun FillerSurface.toGmlName(): String = when (this) {
    is LateralFillerSurface -> "LateralFillerSurface"
    is LongitudinalFillerSurfaceBetweenRoads -> "LongitudinalFillerSurfaceBetweenRoads"
    is LongitudinalFillerSurfaceWithinRoad -> "LongitudinalFillerSurfaceWithinRoad"
}

/**
 * Builder for city objects of the CityGML Transportation module.
 */
class TransportationModuleBuilder(
    val configuration: Roadspaces2CitygmlConfiguration,
    private val identifierAdder: IdentifierAdder
) {
    // Properties and Initializers
    private val _attributesAdder = AttributesAdder(configuration)

    // Methods
    fun createRoad() = Road()
    fun createSection() = Section()
    fun createIntersection() = Intersection()
    fun createMarking() = Marking()

    /**
     * Transforms a [lane] with a [surface] and [centerLine] representation and its [fillerSurfaces] to a
     * CityGML [TrafficSpace] and adds it to the [dstTransportationSpace].
     */
    fun addTrafficSpaceFeature(lane: Lane, surface: AbstractSurface3D, centerLine: AbstractCurve3D, fillerSurfaces: List<FillerSurface>, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()

        val trafficSpaceFeature = createTrafficSpaceFeature(TransportationGranularityValue.LANE)
        identifierAdder.addUniqueIdentifier(lane.id, trafficSpaceFeature)

        // line representation of lane
        val centerLineGeometryTransformer = GeometryTransformer(configuration).also { centerLine.accept(it) }
        trafficSpaceFeature.populateLod2Geometry(centerLineGeometryTransformer)

        // surface representation of lane
        val trafficArea = createTrafficAreaFeature(lane.id, surface).handleReport { report += it }
        // .getOrHandle { report += Message.of(it.message!!, lane.id, isFatal = false, wasHealed = true); return report }
        trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(trafficArea))

        identifierAdder.addIdentifier(lane.id, "Lane", trafficArea)
        _attributesAdder.addAttributes(lane, trafficArea)

        // filler surfaces
        fillerSurfaces.forEach { fillerSurface ->
            val fillerTrafficArea = createTrafficAreaFeature(lane.id, fillerSurface.surface).handleReport { report += it }
            // .getOrHandle { report += Message.of(it.message!!, lane.id, isFatal = false, wasHealed = true); return report }

            identifierAdder.addIdentifier(lane.id, fillerSurface.toGmlName(), fillerTrafficArea)
            _attributesAdder.addAttributes(fillerSurface, fillerTrafficArea)
            trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerTrafficArea))
        }

        // populate transportation space
        val trafficSpaceProperty = TrafficSpaceProperty(trafficSpaceFeature)
        dstTransportationSpace.trafficSpaces.add(trafficSpaceProperty)

        return report
    }

    /**
     * Transforms a [lane] with a [surface] and [centerLine] representation and its [fillerSurfaces] to a
     * CityGML [AuxiliaryTrafficSpace] and adds it to the [dstTransportationSpace].
     */
    fun addAuxiliaryTrafficSpaceFeature(
        lane: Lane,
        surface: AbstractSurface3D,
        centerLine: AbstractCurve3D,
        fillerSurfaces: List<FillerSurface>,
        dstTransportationSpace: AbstractTransportationSpace
    ): Report {
        val report = Report()
        val auxiliaryTrafficSpaceFeature = createAuxiliaryTrafficSpaceFeature(TransportationGranularityValue.LANE)
        identifierAdder.addUniqueIdentifier(lane.id, auxiliaryTrafficSpaceFeature)

        // line representation
        val centerLineGeometryTransformer = GeometryTransformer(configuration).also { centerLine.accept(it) }
        auxiliaryTrafficSpaceFeature.populateLod2Geometry(centerLineGeometryTransformer)

        // surface representation
        val auxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(lane.id, surface)
            .handleReport { report += it }
        // .getOrHandle { report += Message.of(it.message!!, lane.id, isFatal = false, wasHealed = true); return report }
        auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(auxiliaryTrafficArea))

        identifierAdder.addIdentifier(lane.id, "Lane", auxiliaryTrafficArea)
        _attributesAdder.addAttributes(lane, auxiliaryTrafficArea)

        // filler surfaces
        fillerSurfaces.forEach { fillerSurface ->
            val fillerAuxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(lane.id, fillerSurface.surface)
                .handleReport { report += it }
            // .getOrHandle { report += Message.of(it.message!!, lane.id, isFatal = false, wasHealed = true); return report }

            identifierAdder.addIdentifier(lane.id, fillerSurface.toGmlName(), fillerAuxiliaryTrafficArea)
            _attributesAdder.addAttributes(fillerSurface, fillerAuxiliaryTrafficArea)
            auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerAuxiliaryTrafficArea))
        }

        // populate transportation space
        val auxiliaryTrafficSpaceProperty = AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpaceFeature)
        dstTransportationSpace.auxiliaryTrafficSpaces.add(auxiliaryTrafficSpaceProperty)
        return report
    }

    fun addTrafficSpaceFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()
        val trafficSpaceFeature = createTrafficSpaceFeature(TransportationGranularityValue.LANE)

        // surface representation
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        val trafficArea = createTrafficAreaFeature(roadspaceObject.id, geometryTransformer)
            .handleReport { report += it }
        // .getOrHandle { report += Message.of(it.message!!, roadspaceObject.id, isFatal = false, wasHealed = true); return report }
        trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(trafficArea))

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, trafficArea)
        _attributesAdder.addAttributes(roadspaceObject, trafficArea)

        // populate transportation space
        val trafficSpaceProperty = TrafficSpaceProperty(trafficSpaceFeature)
        dstTransportationSpace.trafficSpaces.add(trafficSpaceProperty)
        return report
    }

    fun addAuxiliaryTrafficSpaceFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()
        val auxiliaryTrafficSpaceFeature = createAuxiliaryTrafficSpaceFeature(TransportationGranularityValue.LANE)

        // surface representation
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        val auxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(roadspaceObject.id, geometryTransformer)
            .handleReport { report += it }
        auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(auxiliaryTrafficArea))

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, auxiliaryTrafficArea)
        _attributesAdder.addAttributes(roadspaceObject, auxiliaryTrafficArea)

        // populate transportation space
        val auxiliaryTrafficSpaceProperty = AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpaceFeature)
        dstTransportationSpace.auxiliaryTrafficSpaces.add(auxiliaryTrafficSpaceProperty)
        return report
    }

    fun addMarkingFeature(id: LaneIdentifier, roadMarking: RoadMarking, geometry: AbstractGeometry3D, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()
        val markingFeature = Marking()

        // geometry
        val geometryTransformer = GeometryTransformer(configuration).also { geometry.accept(it) }
        markingFeature.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer)
            .tapLeft { report += Message.of(it.message, id, isFatal = false, wasHealed = true) }

        // semantics
        identifierAdder.addIdentifier(id, "RoadMarking", markingFeature)
        _attributesAdder.addAttributes(id, roadMarking, markingFeature)

        // populate transportation space
        val markingProperty = MarkingProperty(markingFeature)
        dstTransportationSpace.markings.add(markingProperty)
        return report
    }

    fun addMarkingFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace): Report {
        val report = Report()
        val markingFeature = Marking()

        // geometry
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        markingFeature.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer)
            .tapLeft { report += Message.of(it.message, roadspaceObject.id, isFatal = false, wasHealed = true) }

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, markingFeature)
        _attributesAdder.addAttributes(roadspaceObject, markingFeature)

        // populate transportation space
        val markingProperty = MarkingProperty(markingFeature)
        dstTransportationSpace.markings.add(markingProperty)
        return report
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

    private fun createTrafficAreaFeature(id: AbstractRoadspacesIdentifier, abstractGeometry: AbstractGeometry3D): ContextReport<TrafficArea> {
        val geometryTransformer = GeometryTransformer(configuration)
            .also { abstractGeometry.accept(it) }
        return createTrafficAreaFeature(id, geometryTransformer)
    }

    private fun createTrafficAreaFeature(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextReport<TrafficArea> {
        val report = Report()
        val trafficAreaFeature = TrafficArea()

        val solidFaceSelection = listOf(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
        trafficAreaFeature.populateLod2MultiSurfaceFromSolidCutoutOrSurface(geometryTransformer, solidFaceSelection)
            .tapLeft { report += Message.of(it.message, id, isFatal = false, wasHealed = true) }

        return ContextReport(trafficAreaFeature, report)
    }

    private fun createAuxiliaryTrafficAreaFeature(id: AbstractRoadspacesIdentifier, abstractGeometry: AbstractGeometry3D): ContextReport<AuxiliaryTrafficArea> {
        val geometryTransformer = GeometryTransformer(configuration)
            .also { abstractGeometry.accept(it) }
        return createAuxiliaryTrafficAreaFeature(id, geometryTransformer)
    }

    private fun createAuxiliaryTrafficAreaFeature(id: AbstractRoadspacesIdentifier, geometryTransformer: GeometryTransformer): ContextReport<AuxiliaryTrafficArea> {
        val report = Report()
        val auxiliaryTrafficAreaFeature = AuxiliaryTrafficArea()

        val solidFaceSelection = listOf(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
        auxiliaryTrafficAreaFeature.populateLod2MultiSurfaceFromSolidCutoutOrSurface(geometryTransformer, solidFaceSelection)
            .tapLeft { report += Message.of(it.message, id, isFatal = false, wasHealed = true) }

        return ContextReport(auxiliaryTrafficAreaFeature, report)
    }
}
