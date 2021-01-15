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
import org.citygml4j.model.citygml.transportation.*


/**
 * Builder for city objects of the CityGML Transportation module.
 */
class TransportationModuleBuilder(
        val configuration: Roadspaces2CitygmlConfiguration
) {

    enum class Feature { TRACK, ROAD, RAILWAY, SQUARE, NONE }
    enum class Type { TRAFFICAREA, AUXILARYTRAFFICAREA, NONE }

    // Properties and Initializers
    private val _reportLogger = configuration.getReportLogger()


    // Methods
    fun createLaneSurface(surface: AbstractSurface3D): Result<Road, Exception> {
        val geometryTransformer = GeometryTransformer(configuration.parameters, _reportLogger)
                .also { surface.accept(it) }
        val roadObject = Road()

        roadObject.lod2MultiSurface = geometryTransformer.getMultiSurfaceProperty().handleFailure { return it }
        return Result.success(roadObject)
    }

    fun createFillerSurface(surface: AbstractSurface3D): Result<Road, Exception> {
        val geometryTransformer = GeometryTransformer(configuration.parameters, _reportLogger)
                .also { surface.accept(it) }

        val roadObject = Road()
        roadObject.lod2MultiSurface = geometryTransformer.getMultiSurfaceProperty().handleFailure { return it }
        return Result.success(roadObject)
    }

    fun createTransportationComplex(surface: AbstractSurface3D, feature: Feature, type: Type = Type.NONE):
            Result<TransportationComplex, Exception> {

        val geometryTransformer = GeometryTransformer(configuration.parameters, _reportLogger)
                .also { surface.accept(it) }
        return createTransportationComplex(geometryTransformer, feature, type)
    }

    fun createTransportationComplex(geometryTransformer: GeometryTransformer, feature: Feature, type: Type = Type.NONE):
            Result<TransportationComplex, Exception> {

        val transportationComplex = when (feature) {
            Feature.TRACK -> Track()
            Feature.ROAD -> Road()
            Feature.RAILWAY -> Railway()
            Feature.SQUARE -> Square()
            Feature.NONE -> TransportationComplex()
        }

        when (type) {
            Type.TRAFFICAREA -> {
                val trafficArea = TrafficArea().apply {
                    lod2MultiSurface = geometryTransformer.getMultiSurfaceProperty().handleFailure { return it } }
                val trafficAreaProperty = TrafficAreaProperty(trafficArea)
                transportationComplex.trafficArea = listOf(trafficAreaProperty)
            }
            Type.AUXILARYTRAFFICAREA -> {
                val auxiliaryTrafficArea = AuxiliaryTrafficArea().apply {
                    lod2MultiSurface = geometryTransformer.getMultiSurfaceProperty().handleFailure { return it } }
                val auxiliaryTrafficAreaProperty = AuxiliaryTrafficAreaProperty(auxiliaryTrafficArea)
                transportationComplex.auxiliaryTrafficArea = listOf(auxiliaryTrafficAreaProperty)
            }
            else -> transportationComplex.lod2MultiSurface =
                geometryTransformer.getMultiSurfaceProperty().handleFailure { return it }
        }

        return Result.success(transportationComplex)
    }

}
