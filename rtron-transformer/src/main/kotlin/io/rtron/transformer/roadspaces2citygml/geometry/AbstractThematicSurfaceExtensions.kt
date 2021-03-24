/*
 * Copyright 2019-2020 Chair of Geoinformatics, Technical University of Munich
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

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.success
import io.rtron.std.handleFailure
import io.rtron.std.handleSuccess
import org.citygml4j.model.core.AbstractThematicSurface

fun AbstractThematicSurface.populateLod2MultiSurfaceOrLod0Geometry(geometryTransformer: GeometryTransformer): Result<Unit, Exception> {
    val lod2MultiSurfaceError = populateLod2MultiSurface(geometryTransformer).handleSuccess { return it }
    val lod0GeometryError = populateLod0Geometry(geometryTransformer).handleSuccess { return it }

    return Result.error(Exception("No suitable source geometry found for populating the LoD2 multi surface (${lod2MultiSurfaceError.message}) or LoD0 geometry (${lod0GeometryError.message}) of the abstract thematic surface."))
}

/**
 * Populates the [lod] geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Result.success] is returned, if a geometry has been populated; [Result.error], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Result<Unit, Exception> =
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
 * @return [Result.success] is returned, if a geometry has been populated; [Result.error], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod0Geometry(geometryTransformer: GeometryTransformer): Result<Unit, Exception> {
    if (geometryTransformer.isSetMultiSurface())
        geometryTransformer.getMultiSurface().handleFailure { return it }.also { lod0MultiSurface = it; return Result.success(Unit) }
    if (geometryTransformer.isSetMultiCurve())
        geometryTransformer.getMultiCurve().handleFailure { return it }.also { lod0MultiCurve = it; return Result.success(Unit) }

    return Result.error(IllegalStateException("No suitable source geometry found for populating the LoD0 geometry of the abstract thematic surface."))
}

/**
 * Populates the LoD1 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Result.success] is returned, if a geometry has been populated; [Result.error], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod1MultiSurface(geometryTransformer: GeometryTransformer): Result<Unit, Exception> {
    if (geometryTransformer.isSetMultiSurface())
        geometryTransformer.getMultiSurface().handleFailure { return it }.also { lod1MultiSurface = it; return Result.success(Unit) }

    return Result.error(IllegalStateException("No suitable source geometry found for populating the LoD1 multi surface of the abstract thematic surface."))
}

/**
 * Populates the LoD2 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Result.success] is returned, if a geometry has been populated; [Result.error], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod2MultiSurface(geometryTransformer: GeometryTransformer): Result<Unit, Exception> {
    if (geometryTransformer.isSetMultiSurface())
        geometryTransformer.getMultiSurface().handleFailure { return it }.also { lod2MultiSurface = it; return Result.success(Unit) }

    return Result.error(IllegalStateException("No suitable source geometry found for populating the LoD2 multi surface of the abstract thematic surface."))
}

/**
 * Populates the LoD3 geometry of an [AbstractThematicSurface] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @return [Result.success] is returned, if a geometry has been populated; [Result.error], if no adequate geometry could be assigned
 */
fun AbstractThematicSurface.populateLod3MultiSurface(geometryTransformer: GeometryTransformer): Result<Unit, Exception> {
    if (geometryTransformer.isSetMultiSurface())
        geometryTransformer.getMultiSurface().handleFailure { return it }.also { lod3MultiSurface = it; return Result.success(Unit) }

    return Result.error(IllegalStateException("No suitable source geometry found for populating the LoD3 multi surface of the abstract thematic surface."))
}
