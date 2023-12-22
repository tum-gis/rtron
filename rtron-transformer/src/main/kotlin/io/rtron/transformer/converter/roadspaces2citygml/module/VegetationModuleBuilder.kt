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
import arrow.core.getOrElse
import arrow.core.raise.either
import io.rtron.io.issues.ContextIssueList
import io.rtron.io.issues.DefaultIssue
import io.rtron.io.issues.DefaultIssueList
import io.rtron.io.issues.Severity
import io.rtron.model.roadspaces.roadspace.attribute.UnitOfMeasure
import io.rtron.model.roadspaces.roadspace.objects.RoadspaceObject
import io.rtron.transformer.converter.roadspaces2citygml.Roadspaces2CitygmlParameters
import io.rtron.transformer.converter.roadspaces2citygml.geometry.GeometryTransformer
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1Geometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod1ImplicitGeometry
import io.rtron.transformer.converter.roadspaces2citygml.geometry.populateLod2Geometry
import io.rtron.transformer.converter.roadspaces2citygml.transformer.deriveGmlIdentifier
import io.rtron.transformer.issues.roadspaces.of
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

    fun createSolitaryVegetationObjectFeature(roadspaceObject: RoadspaceObject): ContextIssueList<SolitaryVegetationObject> {
        val issueList = DefaultIssueList()

        val solitaryVegetationObjectFeature = SolitaryVegetationObject()

        // geometry
        val pointGeometryTransformer = GeometryTransformer.of(roadspaceObject.pointGeometry, parameters)
        solitaryVegetationObjectFeature.populateLod1ImplicitGeometry(pointGeometryTransformer)
        pointGeometryTransformer.rotation.onSome {
            attributesAdder.addRotationAttributes(it, solitaryVegetationObjectFeature)
        }

        roadspaceObject.boundingBoxGeometry.onSome { currentBoundingBoxGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentBoundingBoxGeometry, parameters)
            solitaryVegetationObjectFeature.populateLod1Geometry(geometryTransformer)
                .mapLeft {
                    issueList += DefaultIssue.of(
                        "NoSuitableGeometryForSolitaryVegetationObjectLod1",
                        it.message,
                        roadspaceObject.id,
                        Severity.WARNING,
                        wasFixed = true
                    )
                }

            addAttributes(solitaryVegetationObjectFeature, geometryTransformer).getOrElse { throw it }
        }

        roadspaceObject.complexGeometry.onSome { currentComplexGeometry ->
            val geometryTransformer = GeometryTransformer.of(currentComplexGeometry, parameters)
            solitaryVegetationObjectFeature.populateLod2Geometry(geometryTransformer)
                .mapLeft {
                    issueList += DefaultIssue.of(
                        "NoSuitableGeometryForSolitaryVegetationObjectLod2",
                        it.message,
                        roadspaceObject.id,
                        Severity.WARNING,
                        wasFixed = true
                    )
                }
        }

        // semantics
        IdentifierAdder.addIdentifier(
            roadspaceObject.id.deriveGmlIdentifier(parameters.gmlIdPrefix),
            solitaryVegetationObjectFeature
        )
        relationAdder.addBelongToRelations(roadspaceObject, solitaryVegetationObjectFeature)
        attributesAdder.addAttributes(roadspaceObject, solitaryVegetationObjectFeature)

        return ContextIssueList(solitaryVegetationObjectFeature, issueList)
    }

    private fun addAttributes(
        solitaryVegetationObjectFeature: SolitaryVegetationObject,
        geometryTransformer: GeometryTransformer
    ): Either<Exception, Unit> = either {
        geometryTransformer.diameter.onSome {
            solitaryVegetationObjectFeature.trunkDiameter = Length(it)
            solitaryVegetationObjectFeature.trunkDiameter.uom = UnitOfMeasure.METER.toGmlString()
        }

        geometryTransformer.height.onSome {
            solitaryVegetationObjectFeature.height = Length(it)
            solitaryVegetationObjectFeature.height.uom = UnitOfMeasure.METER.toGmlString()
        }

        Unit
    }
}
