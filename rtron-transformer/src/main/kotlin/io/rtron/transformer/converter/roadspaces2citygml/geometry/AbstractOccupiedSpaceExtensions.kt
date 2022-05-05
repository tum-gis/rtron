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
import io.rtron.std.handleFailure
import io.rtron.std.handleSuccess
import io.rtron.std.toEither
import io.rtron.std.toResult
import org.citygml4j.model.core.AbstractOccupiedSpace

/**
 * Populates the [lod] geometry of an [AbstractOccupiedSpace], if available. Otherwise the [lod] implicit geometry of the [GeometryTransformer] is populated.
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Either.Right] is returned, if a geometry or implicit geometry has been populated; [Either.Left], if no geometry could be assigned
 */
fun AbstractOccupiedSpace.populateGeometryOrImplicitGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Either<Exception, Unit> {
    val geometryError = populateGeometry(geometryTransformer, lod).toResult().handleSuccess { return it.toEither() }
    val implicitGeometryError = populateImplicitGeometry(geometryTransformer, lod).toResult().handleSuccess { return it.toEither() }

    return Either.Left(Exception("No suitable source geometry found for populating the $lod geometry (${geometryError.message}) or implicit geometry (${implicitGeometryError.message}) of the abstract occupied space."))
}

/**
 * Populates the [lod] implicit geometry of an [AbstractOccupiedSpace] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Either.Right] is returned, if an implicit geometry has been populated; [Either.Left], if no implicit geometry could be assigned
 */
fun AbstractOccupiedSpace.populateImplicitGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Either<Exception, Unit> {
    if (geometryTransformer.isSetImplicitGeometry())
        when (lod) {
            LevelOfDetail.ZERO -> throw IllegalArgumentException("An implicit geometry can not be assigned to an AbstractOccupiedSpace at level 0.")
            LevelOfDetail.ONE -> geometryTransformer.getImplicitGeometry().toResult().handleFailure { return Either.Left(it.error) }.also { lod1ImplicitRepresentation = it; return Either.Right(Unit) }
            LevelOfDetail.TWO -> geometryTransformer.getImplicitGeometry().toResult().handleFailure { return Either.Left(it.error) }.also { lod2ImplicitRepresentation = it; return Either.Right(Unit) }
            LevelOfDetail.THREE -> geometryTransformer.getImplicitGeometry().toResult().handleFailure { return Either.Left(it.error) }.also { lod3ImplicitRepresentation = it; return Either.Right(Unit) }
        }

    return Either.Left(IllegalStateException("No suitable source geometry found for populating the LoD $lod implicit geometry of the abstract occupied space."))
}
