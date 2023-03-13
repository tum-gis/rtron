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
import arrow.core.right
import org.citygml4j.core.model.core.AbstractThematicSurface

fun AbstractThematicSurface.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> {
    val lod2MultiSurfaceError = populateLod2MultiSurface(geometryTransformer).fold({ it }, { return it.right() })
    val lod0GeometryError = populateLod0Geometry(geometryTransformer).fold({ it }, { return it.right() })

    return GeometryTransformerException.NoSuiteableSourceGeometry("No suitable source geometry found for populating the LoD2 multi surface (${lod2MultiSurfaceError.message}) or LoD0 geometry (${lod0GeometryError.message}) of the abstract thematic surface.").left()
}

/**
 * Populates the [lod] geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Either<GeometryTransformerException, Unit> =
    when (lod) {
        LevelOfDetail.ZERO -> populateLod0Geometry(geometryTransformer)
        LevelOfDetail.ONE -> populateLod1MultiSurface(geometryTransformer)
        LevelOfDetail.TWO -> populateLod2MultiSurface(geometryTransformer)
        LevelOfDetail.THREE -> populateLod3MultiSurface(geometryTransformer)
    }

/**
 * Populates the LoD0 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod0Geometry(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getMultiSurface().tap { currentMultiCurveResult ->
        lod0MultiSurface = currentMultiCurveResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    geometryTransformer.getMultiCurve().tap { currentMultiCurveResult ->
        lod0MultiCurve = currentMultiCurveResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LoD0 geometry of the abstract thematic surface.").left().bind()
}

/**
 * Populates the LoD1 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod1MultiSurface(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getMultiSurface().tap { currentMultiSurfaceResult ->
        lod1MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LoD1 multi surface of the abstract thematic surface.").left().bind()
}

/**
 * Populates the LoD2 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod2MultiSurface(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getMultiSurface().tap { currentMultiSurfaceResult ->
        lod2MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LoD2 multi surface of the abstract thematic surface.").left().bind()
}

/**
 * Populates the LoD3 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod3MultiSurface(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getMultiSurface().tap { currentMultiSurfaceResult ->
        lod3MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LoD3 multi surface of the abstract thematic surface.").left().bind()
}

fun AbstractThematicSurface.populateLod2MultiSurfaceFromSolidCutoutOrSurface(geometryTransformer: GeometryTransformer, solidFaceSelection: List<GeometryTransformer.FaceType>): Either<GeometryTransformerException, Unit> = either.eager {
    geometryTransformer.getSolidCutoutOrSurface(*solidFaceSelection.toTypedArray()).tap { currentMultiSurfaceResult ->
        lod2MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@eager
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LoD2 multi surface of the abstract thematic surface.").left().bind()
}
