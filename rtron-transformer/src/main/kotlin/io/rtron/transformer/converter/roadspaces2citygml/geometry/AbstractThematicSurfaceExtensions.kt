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
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import org.citygml4j.core.model.core.AbstractThematicSurface

fun AbstractThematicSurface.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> {
    val lod2MultiSurfaceError = populateLod2MultiSurface(geometryTransformer).fold({ it }, { return it.right() })
    val lod0GeometryError = populateLod0Geometry(geometryTransformer).fold({ it }, { return it.right() })

    return GeometryTransformerException.NoSuiteableSourceGeometry("LOD2 multi surface or LOD0 geometry of the abstract thematic surface.").left()
}

/**
 * Populates the LOD0 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod0Geometry(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either {
    geometryTransformer.getMultiSurface().onSome { currentMultiCurveResult ->
        lod0MultiSurface = currentMultiCurveResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@either
    }

    geometryTransformer.getMultiCurve().onSome { currentMultiCurveResult ->
        lod0MultiCurve = currentMultiCurveResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@either
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD0 geometry of the abstract thematic surface.").left()
        .bind()
}

/**
 * Populates the LOD1 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod1MultiSurface(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either {
    geometryTransformer.getMultiSurface().onSome { currentMultiSurfaceResult ->
        lod1MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@either
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD1 multi surface of the abstract thematic surface.")
        .left().bind()
}

/**
 * Populates the LOD2 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod2MultiSurface(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either {
    geometryTransformer.getMultiSurface().onSome { currentMultiSurfaceResult ->
        lod2MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@either
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD2 multi surface of the abstract thematic surface.")
        .left().bind()
}

/**
 * Populates the LOD3 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Either.Right] is returned, if a geometry has been populated; [Either.Left], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod3MultiSurface(geometryTransformer: GeometryTransformer): Either<GeometryTransformerException, Unit> = either {
    geometryTransformer.getMultiSurface().onSome { currentMultiSurfaceResult ->
        lod3MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
        return@either
    }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD3 multi surface of the abstract thematic surface.")
        .left().bind()
}

fun AbstractThematicSurface.populateLod2MultiSurfaceFromSolidCutoutOrSurface(geometryTransformer: GeometryTransformer, solidFaceSelection: List<GeometryTransformer.FaceType>): Either<GeometryTransformerException, Unit> = either {
    geometryTransformer.getSolidCutoutOrSurface(*solidFaceSelection.toTypedArray())
        .onSome { currentMultiSurfaceResult ->
            lod2MultiSurface = currentMultiSurfaceResult.mapLeft { it.toGeometryGenerationException() }.bind()
            return@either
        }

    GeometryTransformerException.NoSuiteableSourceGeometry("LOD2 multi surface of the abstract thematic surface.")
        .left().bind()
}
