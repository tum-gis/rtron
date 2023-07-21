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

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.getOrElse
import io.rtron.io.messages.ContextMessageList
import io.rtron.io.messages.DefaultMessage
import io.rtron.io.messages.DefaultMessageList
import io.rtron.io.messages.Severity
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1Geometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1ImplicitGeometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.messages.roadspaces.of
import org.citygml4j.core.model.vegetation.SolitaryVegetationObject
import org.xmlobjects.gml.model.measures.Length

/**
 * Builder for city objects of the CityGML Vegetation module.
 */
class VegetationModuleBuilder(
    private val parameters: Roadspaces2CitygmlParameters
) {
    // Properties and Initializers
    private val relationAdder = RelationAdder(parameters)
    private val attributesAdder = AttributesAdder(parameters)

    // Methods

    fun createSolitaryVegetationObjectFeature(roadspaceObject: RoadspaceObject): ContextMessageList<SolitaryVegetationObject> {
        val messageList = DefaultMessageList()

        val solitaryVegetationObjectFeature = SolitaryVegetationObject()

        // geometry
        val pointGeometryTransformer = GeometryTransformer.of(roadspaceObject.pointGeometry, parameters)
        solitaryVegetationObjectFeature.populateLod1ImplicitGeometry(pointGeometryTransformer)
        pointGeometryTransformer.rotation.tap {
            attributesAdder.addRotationAttributes(it, solitaryVegetationObjectFeature)
        }

        roadspaceObject.boundingBoxGeometry.tap { currentBoundingBoxGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentBoundingBoxGeometry, parameters)
            solitaryVegetationObjectFeature.populateLod1Geometry(geometryTransformer)
                .mapLeft { messageList += DefaultMessage.of("NoSuitableGeometryForSolitaryVegetationObjectLod1", it.message, roadspaceObject.id, Severity.WARNING, wasFixed = true) }

            addAttributes(solitaryVegetationObjectFeature, geometryTransformer).getOrElse { throw it }
        }

        roadspaceObject.complexGeometry.tap { currentComplexGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentComplexGeometry, parameters)
            solitaryVegetationObjectFeature.populateLod2Geometry(geometryTransformer)
                .mapLeft { messageList += DefaultMessage.of("NoSuitableGeometryForSolitaryVegetationObjectLod2", it.message, roadspaceObject.id, Severity.WARNING, wasFixed = true) }
        }

        // semantics
        IdentifierAdder.addIdentifier(roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix), solitaryVegetationObjectFeature)
        relationAdder.addBelongToRelations(roadspaceObject, solitaryVegetationObjectFeature)
        attributesAdder.addAttributes(roadspaceObject, solitaryVegetationObjectFeature)

        return ContextMessageList(solitaryVegetationObjectFeature, messageList)
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
