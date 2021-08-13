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

package io.rtron.transformer.roadspaces2citygml.module

import com.github.kittinunf.result.Result
import io.rtron.io.logging.LogManager
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.std.handleFailure
import io.rtron.transformer.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import org.citygml4j.model.vegetation.SolitaryVegetationObject
import org.xmlobjects.gml.model.measures.Length

/**
 * Builder for city objects of the CityGML Vegetation module.
 */
class VegetationModuleBuilder(
    private val configuration: Roadspaces2CitygmlConfiguration,
    private val identifierAdder: IdentifierAdder
) {
    // Properties and Initializers
    private val _reportLogger = LogManager.getReportLogger(configuration.projectId)
    private val _attributesAdder = AttributesAdder(configuration)

    // Methods

    fun createSolitaryVegetationFeature(roadspaceObject: RoadspaceObject): Result<SolitaryVegetationObject, Exception> {
        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        val solitaryVegetationObjectFeature = SolitaryVegetationObject()

        solitaryVegetationObjectFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.TWO).handleFailure { return it }
        if (geometryTransformer.isSetRotation())
            geometryTransformer.getRotation().handleFailure { return it }.also { _attributesAdder.addRotationAttributes(it, solitaryVegetationObjectFeature) }
        addAttributes(solitaryVegetationObjectFeature, geometryTransformer).handleFailure { return it }

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, solitaryVegetationObjectFeature)
        _attributesAdder.addAttributes(roadspaceObject, solitaryVegetationObjectFeature)

        return Result.success(solitaryVegetationObjectFeature)
    }

    private fun addAttributes(
        solitaryVegetationObjectFeature: SolitaryVegetationObject,
        geometryTransformer: GeometryTransformer
    ): Result<Unit, Exception> {

        if (geometryTransformer.isSetDiameter())
            geometryTransformer.getDiameter().handleFailure { return it }.also {
                solitaryVegetationObjectFeature.trunkDiameter = Length(it)
                solitaryVegetationObjectFeature.trunkDiameter.uom = UnitOfMeasure.METER.toGmlString()
            }

        if (geometryTransformer.isSetHeight())
            geometryTransformer.getHeight().handleFailure { return it }.also {
                solitaryVegetationObjectFeature.height = Length(it)
                solitaryVegetationObjectFeature.height.uom = UnitOfMeasure.METER.toGmlString()
            }

        return Result.success(Unit)
    }
}
