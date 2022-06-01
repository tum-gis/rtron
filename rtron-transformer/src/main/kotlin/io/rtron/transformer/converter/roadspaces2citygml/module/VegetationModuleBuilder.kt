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

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import arrow.core.continuations.either
import io.rtron.io.report.ContextReport
import io.rtron.io.report.Message
import io.rtron.io.report.Report
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.configuration.Roadspaces2CitygmlConfiguration
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.LevelOfDetail
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateGeometryOrImplicitGeometry
import io.rtron.transformer.report.of
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
    private val _attributesAdder = AttributesAdder(configuration)

    // Methods

    fun createSolitaryVegetationFeature(roadspaceObject: RoadspaceObject): ContextReport<SolitaryVegetationObject> {
        val report = Report()

        val geometryTransformer = GeometryTransformer.of(roadspaceObject, configuration)
        val solitaryVegetationObjectFeature = SolitaryVegetationObject()

        solitaryVegetationObjectFeature.populateGeometryOrImplicitGeometry(geometryTransformer, LevelOfDetail.TWO)
            .mapLeft { report += Message.of(it.message, roadspaceObject.id, isFatal = false, wasHealed = true) }
        geometryTransformer.rotation.tap {
            _attributesAdder.addRotationAttributes(it, solitaryVegetationObjectFeature)
        }
        addAttributes(solitaryVegetationObjectFeature, geometryTransformer).bind()

        // semantics
        identifierAdder.addUniqueIdentifier(roadspaceObject.id, solitaryVegetationObjectFeature)
        _attributesAdder.addAttributes(roadspaceObject, solitaryVegetationObjectFeature)

        return ContextReport(solitaryVegetationObjectFeature, report)
    }

    private fun addAttributes(
        solitaryVegetationObjectFeature: SolitaryVegetationObject,
        geometryTransformer: GeometryTransformer
    ): Either<Exception, Unit> = either.eager {

        geometryTransformer.diameter.tap {
            solitaryVegetationObjectFeature.trunkDiameter = Length(it)
            solitaryVegetationObjectFeature.trunkDiameter.uom = UnitOfMeasure.METER.toGmlString()
        }

        geometryTransformer.height.tap {
            solitaryVegetationObjectFeature.height = Length(it)
            solitaryVegetationObjectFeature.height.uom = UnitOfMeasure.METER.toGmlString()
        }

        Unit
    }
}
