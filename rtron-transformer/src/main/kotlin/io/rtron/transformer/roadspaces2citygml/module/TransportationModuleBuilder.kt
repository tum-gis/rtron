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

package io.rtron.transformer.roadspaces2citygml.module

import com.github.kittinunf.result.Result
import io.rtron.io.logging.LogManager
import io.rtron.math.geometry.euclidean.threed.AbstractGeometry3D
import io.rtron.math.geometry.euclidean.threed.curve.AbstractCurve3D
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.model.roadspaces.common.FillerSurface
import io.rtron.model.roadspaces.common.LateralFillerSurface
import io.rtron.model.roadspaces.common.LongitudinalFillerSurfaceBetweenRoads
import io.rtron.model.roadspaces.common.LongitudinalFillerSurfaceWithinRoad
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.model.roadspaces.roadspace.road.Lane
import io.rtron.model.roadspaces.roadspace.road.LaneIdentifier
import io.rtron.model.roadspaces.roadspace.road.RoadMarking
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.roadspaces2citygml.geometry.populateLod2MultiSurfaceOrLod0Geometry
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
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)
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
    fun addTrafficSpaceFeature(lane: Lane, surface: AbstractSurface3D, centerLine: AbstractCurve3D, fillerSurfaces: List<FillerSurface>, dstTransportationSpace: AbstractTransportationSpace) {
        val trafficSpaceFeature = createTrafficSpaceFeature(TransportationGranularityValue.LANE)
        identifierAdder.addUniqueIdentifier(lane.id, trafficSpaceFeature)

        // line representation of lane
        val centerLineGeometryTransformer = GeometryTransformer(configuration).also { centerLine.accept(it) }
        trafficSpaceFeature.populateLod2Geometry(centerLineGeometryTransformer)

        // surface representation of lane
        val trafficArea = createTrafficAreaFeature(surface)
            .handleFailure { _reportLogger.log(it, lane.id.toString()); return }
        trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(trafficArea))

        identifierAdder.addIdentifier(lane.id, "Lane", trafficArea)
        _attributesAdder.addAttributes(lane, trafficArea)

        // filler surfaces
        fillerSurfaces.forEach { fillerSurface ->
            val fillerTrafficArea = createTrafficAreaFeature(fillerSurface.surface)
                .handleFailure { _reportLogger.log(it, lane.id.toString()); return }

            identifierAdder.addIdentifier(lane.id, fillerSurface.toGmlName(), fillerTrafficArea)
            _attributesAdder.addAttributes(fillerSurface, fillerTrafficArea)
            trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerTrafficArea))
        }

        // populate transportation space
        val trafficSpaceProperty = TrafficSpaceProperty(trafficSpaceFeature)
        dstTransportationSpace.trafficSpaces.add(trafficSpaceProperty)
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
    ) {
        val auxiliaryTrafficSpaceFeature = createAuxiliaryTrafficSpaceFeature(TransportationGranularityValue.LANE)
        identifierAdder.addUniqueIdentifier(lane.id, auxiliaryTrafficSpaceFeature)

        // line representation
        val centerLineGeometryTransformer = GeometryTransformer(configuration).also { centerLine.accept(it) }
        auxiliaryTrafficSpaceFeature.populateLod2Geometry(centerLineGeometryTransformer)

        // surface representation
        val auxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(surface)
            .handleFailure { _reportLogger.log(it, lane.id.toString()); return }
        auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(auxiliaryTrafficArea))

        identifierAdder.addIdentifier(lane.id, "Lane", auxiliaryTrafficArea)
        _attributesAdder.addAttributes(lane, auxiliaryTrafficArea)

        // filler surfaces
        fillerSurfaces.forEach { fillerSurface ->
            val fillerAuxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(fillerSurface.surface)
                .handleFailure { _reportLogger.log(it, lane.id.toString()); return }

            identifierAdder.addIdentifier(lane.id, fillerSurface.toGmlName(), fillerAuxiliaryTrafficArea)
            _attributesAdder.addAttributes(fillerSurface, fillerAuxiliaryTrafficArea)
            auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(fillerAuxiliaryTrafficArea))
        }

        // populate transportation space
        val auxiliaryTrafficSpaceProperty = AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpaceFeature)
        dstTransportationSpace.auxiliaryTrafficSpaces.add(auxiliaryTrafficSpaceProperty)
    }

    fun addTrafficSpaceFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace) {
        val trafficSpaceFeature = createTrafficSpaceFeature(TransportationGranularityValue.LANE)

        // surface representation
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        val trafficArea = createTrafficAreaFeature(geometryTransformer)
            .handleFailure { _reportLogger.log(it, roadspaceObject.id.toString()); return }
        trafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(trafficArea))

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, trafficArea)
        _attributesAdder.addAttributes(roadspaceObject, trafficArea)

        // populate transportation space
        val trafficSpaceProperty = TrafficSpaceProperty(trafficSpaceFeature)
        dstTransportationSpace.trafficSpaces.add(trafficSpaceProperty)
    }

    fun addAuxiliaryTrafficSpaceFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace) {
        val auxiliaryTrafficSpaceFeature = createAuxiliaryTrafficSpaceFeature(TransportationGranularityValue.LANE)

        // surface representation
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        val auxiliaryTrafficArea = createAuxiliaryTrafficAreaFeature(geometryTransformer)
            .handleFailure { _reportLogger.log(it, roadspaceObject.id.toString()); return }
        auxiliaryTrafficSpaceFeature.addBoundary(AbstractSpaceBoundaryProperty(auxiliaryTrafficArea))

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, auxiliaryTrafficArea)
        _attributesAdder.addAttributes(roadspaceObject, auxiliaryTrafficArea)

        // populate transportation space
        val auxiliaryTrafficSpaceProperty = AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpaceFeature)
        dstTransportationSpace.auxiliaryTrafficSpaces.add(auxiliaryTrafficSpaceProperty)
    }

    fun addMarkingFeature(id: LaneIdentifier, roadMarking: RoadMarking, geometry: AbstractGeometry3D, dstTransportationSpace: AbstractTransportationSpace) {
        val markingFeature = Marking()

        // geometry
        val geometryTransformer = GeometryTransformer(configuration).also { geometry.accept(it) }
        markingFeature.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer)

        // semantics
        identifierAdder.addIdentifier(id, "RoadMarking", markingFeature)
        _attributesAdder.addAttributes(id, roadMarking, markingFeature)

        // populate transportation space
        val markingProperty = MarkingProperty(markingFeature)
        dstTransportationSpace.markings.add(markingProperty)
    }

    fun addMarkingFeature(roadspaceObject: RoadspaceObject, dstTransportationSpace: AbstractTransportationSpace) {
        val markingFeature = Marking()

        // geometry
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        markingFeature.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer)

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, markingFeature)
        _attributesAdder.addAttributes(roadspaceObject, markingFeature)

        // populate transportation space
        val markingProperty = MarkingProperty(markingFeature)
        dstTransportationSpace.markings.add(markingProperty)
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

    private fun createTrafficAreaFeature(abstractGeometry: AbstractGeometry3D): Result<TrafficArea, Exception> {
        val geometryTransformer = GeometryTransformer(configuration)
            .also { abstractGeometry.accept(it) }
        return createTrafficAreaFeature(geometryTransformer)
    }

    private fun createTrafficAreaFeature(geometryTransformer: GeometryTransformer): Result<TrafficArea, Exception> {
        val trafficAreaFeature = TrafficArea()
        trafficAreaFeature.lod2MultiSurface = geometryTransformer
            .getSolidCutoutOrSurface(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
            .handleFailure { return it }
        return Result.success(trafficAreaFeature)
    }

    private fun createAuxiliaryTrafficAreaFeature(abstractGeometry: AbstractGeometry3D): Result<AuxiliaryTrafficArea, Exception> {
        val geometryTransformer = GeometryTransformer(configuration)
            .also { abstractGeometry.accept(it) }
        return createAuxiliaryTrafficAreaFeature(geometryTransformer)
    }

    private fun createAuxiliaryTrafficAreaFeature(geometryTransformer: GeometryTransformer):
        Result<AuxiliaryTrafficArea, Exception> {
        val auxiliaryTrafficAreaFeature = AuxiliaryTrafficArea()
        auxiliaryTrafficAreaFeature.lod2MultiSurface = geometryTransformer
            .getSolidCutoutOrSurface(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE)
            .handleFailure { return it }
        return Result.success(auxiliaryTrafficAreaFeature)
    }
}
