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

package io.rtron.transformer.converter.roadspaces2citygml.geometry

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import arrow.core.right
import org.citygml4j.core.model.core.AbstractOccupiedSpace

/**
 * Populates the [lod] geometry of an [AbstractOccupiedSpace], if available. Otherwise the [lod] implicit geometry of the [GeometryTransformer] is populated.
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Either.Right] is returned, if a geometry or implicit geometry has been populated; [Either.Left], if no geometry could be assigned
 */
fun AbstractOccupiedSpace.populateGeometryOrImplicitGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Either<GeometryTransformerException, Unit> = either.eager {
    val geometryErrorMessage: String = populateGeometry(geometryTransformer, lod)
        .tap { right().bind() }
        .fold({ it.message }, { "" })
    val implicitGeometryErrorMessage: String = populateImplicitGeometry(geometryTransformer, lod)
        .tap { right().bind() }
        .fold({ it.message }, { "" })

    GeometryTransformerException.NoSuiteableSourceGeometry(
        "No suitable source geometry found for populating the $lod geometry ($geometryErrorMessage) " +
            "or implicit geometry ($implicitGeometryErrorMessage) of the abstract occupied space."
    ).left().bind()
}

/**
 * Populates the [lod] implicit geometry of an [AbstractOccupiedSpace] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Either.Right] is returned, if an implicit geometry has been populated; [Either.Left], if no implicit geometry could be assigned
 */
fun AbstractOccupiedSpace.populateImplicitGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Either<GeometryTransformerException, Unit> = either.eager {
    require(lod != LevelOfDetail.ZERO) { "An implicit geometry can not be assigned to an AbstractOccupiedSpace at level 0." }

    geometryTransformer.getImplicitGeometry().tap { currentImplicitGeometryProperty ->
        if (lod == LevelOfDetail.ONE) {
            lod1ImplicitRepresentation = currentImplicitGeometryProperty
            return@eager
        }
        if (lod == LevelOfDetail.TWO) {
            lod2ImplicitRepresentation = currentImplicitGeometryProperty
            return@eager
        }
        if (lod == LevelOfDetail.THREE) {
            lod3ImplicitRepresentation = currentImplicitGeometryProperty
            return@eager
        }
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("No suitable source geometry found for populating the LoD $lod implicit geometry of the abstract occupied space.").left().bind()
}
