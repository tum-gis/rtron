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

package io.rtron.transformer.roadspaces2citygml.geometry

import arrow.core.Either
import io.rtron.std.handleFailure
import io.rtron.std.toResult
import org.citygml4j.model.core.AbstractSpace

/**
 * Populates the [lod] geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Either<Exception, Unit> =
    when (lod) {
        LevelOfDetail.ZERO -> populateLod0Geometry(geometryTransformer)
        LevelOfDetail.ONE -> populateLod1Geometry(geometryTransformer)
        LevelOfDetail.TWO -> populateLod2Geometry(geometryTransformer)
        LevelOfDetail.THREE -> populateLod3Geometry(geometryTransformer)
    }

/**
 * Populates the LoD0 point geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod0Geometry(geometryTransformer: GeometryTransformer): Either<Exception, Unit> {
    if (geometryTransformer.isSetPoint())
        geometryTransformer.getPoint().toResult().handleFailure { return Either.Left(it.error) }.also { lod0Point = it; return Either.Right(Unit) }

    return Either.Left(IllegalStateException("No suitable source geometry found for populating the LoD0 geometry of the abstract space."))
}

/**
 * Populates the LoD1 geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 * So only the solid geometry are populated at LoD1 (since multiSurface, multiCurve are not available at this LoD).
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod1Geometry(geometryTransformer: GeometryTransformer): Either<Exception, Unit> {
    if (geometryTransformer.isSetSolid())
        geometryTransformer.getSolid().toResult().handleFailure { return Either.Left(it.error) }.also { lod1Solid = it; return Either.Right(Unit) }

    return Either.Left(IllegalStateException("No suitable source geometry found for populating the LoD1 geometry of the abstract space."))
}

/**
 * Populates the LoD2 geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 * Only the first available geometry type is populated, with the prioritization order being: solid, multiSurface, multiCurve.
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod2Geometry(geometryTransformer: GeometryTransformer): Either<Exception, Unit> {
    if (geometryTransformer.isSetSolid())
        geometryTransformer.getSolid().toResult().handleFailure { return Either.Left(it.error) }.also { lod2Solid = it; return Either.Right(Unit) }
    if (geometryTransformer.isSetMultiSurface())
        geometryTransformer.getMultiSurface().toResult().handleFailure { return Either.Left(it.error) }.also { lod2MultiSurface = it; return Either.Right(Unit) }
    if (geometryTransformer.isSetMultiCurve())
        geometryTransformer.getMultiCurve().toResult().handleFailure { return Either.Left(it.error) }.also { lod2MultiCurve = it; return Either.Right(Unit) }

    return Either.Left(IllegalStateException("No suitable source geometry found for populating the LoD2 geometry of the abstract space."))
}

/**
 * Populates the LoD2 geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 * Only the first available geometry type is populated, with the prioritization order being: solid, multiSurface, multiCurve.
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod3Geometry(geometryTransformer: GeometryTransformer): Either<Exception, Unit> {
    if (geometryTransformer.isSetSolid())
        geometryTransformer.getSolid().toResult().handleFailure { return Either.Left(it.error) }.also { lod3Solid = it; return Either.Right(Unit) }
    if (geometryTransformer.isSetMultiSurface())
        geometryTransformer.getMultiSurface().toResult().handleFailure { return Either.Left(it.error) }.also { lod3MultiSurface = it; return Either.Right(Unit) }
    if (geometryTransformer.isSetMultiCurve())
        geometryTransformer.getMultiCurve().toResult().handleFailure { return Either.Left(it.error) }.also { lod3MultiCurve = it; return Either.Right(Unit) }

    return Either.Left(IllegalStateException("No suitable source geometry found for populating the LoD3 geometry of the abstract space."))
}
