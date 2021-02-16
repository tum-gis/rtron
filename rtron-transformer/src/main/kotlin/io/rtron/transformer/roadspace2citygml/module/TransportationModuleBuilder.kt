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

package io.rtron.transformer.roadspace2citygml.module

import com.github.kittinunf.result.Result
import io.rtron.math.geometry.euclidean.threed.surface.AbstractSurface3D
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspace2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import org.citygml4j.model.core.AbstractSpaceBoundaryProperty
import org.citygml4j.model.transportation.AbstractTransportationSpace
import org.citygml4j.model.transportation.AuxiliaryTrafficArea
import org.citygml4j.model.transportation.AuxiliaryTrafficSpace
import org.citygml4j.model.transportation.AuxiliaryTrafficSpaceProperty
import org.citygml4j.model.transportation.GranularityValue
import org.citygml4j.model.transportation.Railway
import org.citygml4j.model.transportation.Road
import org.citygml4j.model.transportation.Square
import org.citygml4j.model.transportation.Track
import org.citygml4j.model.transportation.TrafficArea
import org.citygml4j.model.transportation.TrafficSpace
import org.citygml4j.model.transportation.TrafficSpaceProperty

/**
 * Builder for city objects of the CityGML Transportation module.
 */
class TransportationModuleBuilder(
    val configuration: Roadspaces2CitygmlConfiguration
) {

    enum class Feature { TRACK, ROAD, RAILWAY, SQUARE }
    enum class Type { TRAFFICAREA, AUXILARYTRAFFICAREA, NONE }

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()

    // Methods
    fun createLaneSurface(surface: AbstractSurface3D): Result<Road, Exception> {
        val geometryTransformer = GeometryTransformer(configuration.parameters, _reportLogger)
            .also { surface.accept(it) }
        val roadObject = Road()

        roadObject.lod2MultiSurface = geometryTransformer.getMultiSurface().handleFailure { return it }
        return Result.success(roadObject)
    }

    fun createFillerSurface(surface: AbstractSurface3D): Result<Road, Exception> {
        val geometryTransformer = GeometryTransformer(configuration.parameters, _reportLogger)
            .also { surface.accept(it) }

        val roadObject = Road()
        roadObject.lod2MultiSurface = geometryTransformer.getMultiSurface().handleFailure { return it }
        return Result.success(roadObject)
    }

    fun createTransportationSpace(geometryTransformer: GeometryTransformer, feature: Feature, type: Type = Type.NONE):
        Result<AbstractTransportationSpace, Exception> {

            val transportationSpace = when (feature) {
                Feature.TRACK -> Track()
                Feature.ROAD -> Road()
                Feature.RAILWAY -> Railway()
                Feature.SQUARE -> Square()
            }

            when (type) {
                Type.TRAFFICAREA -> {
                    val trafficArea = TrafficArea()
                    trafficArea.lod2MultiSurface = geometryTransformer.getSolidCutoutOrSurface(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE).handleFailure { return it }
                    val spaceBoundary = AbstractSpaceBoundaryProperty(trafficArea)

                    val trafficSpace = TrafficSpace().apply { addBoundary(spaceBoundary) }
                    trafficSpace.granularity = GranularityValue.LANE
                    transportationSpace.trafficSpaces = listOf(TrafficSpaceProperty(trafficSpace))
                }
                Type.AUXILARYTRAFFICAREA -> {
                    val auxiliaryTrafficArea = AuxiliaryTrafficArea()
                    auxiliaryTrafficArea.lod2MultiSurface = geometryTransformer.getSolidCutoutOrSurface(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE).handleFailure { return it }
                    val spaceBoundary = AbstractSpaceBoundaryProperty(auxiliaryTrafficArea)

                    val auxiliaryTrafficSpace = AuxiliaryTrafficSpace().apply { addBoundary(spaceBoundary) }
                    auxiliaryTrafficSpace.granularity = GranularityValue.LANE
                    transportationSpace.auxiliaryTrafficSpaces = listOf(AuxiliaryTrafficSpaceProperty(auxiliaryTrafficSpace))
                }
                else ->
                    transportationSpace.lod2MultiSurface =
                        geometryTransformer.getSolidCutoutOrSurface(GeometryTransformer.FaceType.TOP, GeometryTransformer.FaceType.SIDE).handleFailure { return it }
            }

            return Result.success(transportationSpace)
        }
}
