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
import com.github.kittinunf.result.success
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspace2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspace2citygml.parameter.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspace2citygml.transformer.AttributesAdder
import io.rtron.transformer.roadspace2citygml.transformer.toGmlString
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject
import org.citygml4j.model.gml.measures.Length

/**
 * Builder for city objects of the CityGML Vegetation module.
 */
class VegetationModuleBuilder(
    val configuration: Roadspaces2CitygmlConfiguration
) {

    // Properties and Initializers
    private val _attributesAdder = AttributesAdder(configuration.parameters)

    // Methods

    fun createVegetationObject(geometryTransformer: GeometryTransformer): Result<SolitaryVegetationObject, Exception> {
        val solitaryVegetationObject = SolitaryVegetationObject()

        solitaryVegetationObject.lod1Geometry = geometryTransformer
            .getGeometryProperty()
            .handleFailure { return it }
        geometryTransformer.getRotation().success { _attributesAdder.addRotationAttributes(it, solitaryVegetationObject) }

        addAttributes(solitaryVegetationObject, geometryTransformer)
        return Result.success(solitaryVegetationObject)
    }

    private fun addAttributes(
        solitaryVegetationObject: SolitaryVegetationObject,
        geometryTransformer: GeometryTransformer
    ) {

        geometryTransformer.getDiameter().success {
            solitaryVegetationObject.trunkDiameter = Length(it)
            solitaryVegetationObject.trunkDiameter.uom = UnitOfMeasure.METER.toGmlString()
        }

        geometryTransformer.getHeight().success {
            solitaryVegetationObject.height = Length(it)
            solitaryVegetationObject.height.uom = UnitOfMeasure.METER.toGmlString()
        }
    }
}
