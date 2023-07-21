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

package io.rtron.transformer.converter.roadspaces2citygml.geometry

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.left
import org.citygml4j.core.model.core.AbstractSpace

/**
 * Populates the LOD0 point geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod0Geometry(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getPoint().tap {
        lod0Point = it
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD0 geometry of the abstract space.").left().bind()
}

/**
 * Populates the LOD1 geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 * So only the solid geometry are populated at LOD1 (since multiSurface, multiCurve are not available at this LOD).
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod1Geometry(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getSolid().tap {
        lod1Solid = it
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD1 geometry of the abstract space.").left().bind()
}

/**
 * Populates the LOD2 geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 * Only the first available geometry type is populated, with the prioritization order being: solid, multiSurface, multiCurve.
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod2Geometry(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getSolid().tap {
        lod2Solid = it
        return@eager
    }

    geometryTransformer.getMultiSurface().tap { currentMultiSurface ->
        lod2MultiSurface = currentMultiSurface.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    geometryTransformer.getMultiCurve().tap { currentMultiSurfaceResult ->
        lod2MultiCurve = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD2 geometry of the abstract space.").left().bind()
}

/**
 * Populates the LOD2 geometry of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 * Only the first available geometry type is populated, with the prioritization order being: solid, multiSurface, multiCurve.
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod3Geometry(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getSolid().tap {
        lod3Solid = it
        return@eager
    }

    geometryTransformer.getMultiSurface().tap { currentMultiSurfaceResult ->
        lod3MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    geometryTransformer.getMultiCurve().tap { currentMultiCurveResult ->
        lod3MultiCurve = currentMultiCurveResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD3 geometry of the abstract space").left().bind()
}
