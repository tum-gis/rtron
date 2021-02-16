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

package io.rtron.transformer.roadspace2citygml.geometry

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.success
import org.citygml4j.model.core.AbstractSpace

/**
 * Populates the LoD2 geometries of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 * Only the first available geometry type is populated, with the prioritization order being: solid, multiSurface, multiCurve.
 * The point at LoD0 serves as accepted fallback.
 *
 * @param geometryTransformer source geometries
 * @return [Result.success] is returned, if a geometry has been populated; [Result.error], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod2Geometries(geometryTransformer: GeometryTransformer): Result<Unit, IllegalStateException> {
    geometryTransformer.getSolid().success { lod2Solid = it; return Result.success(Unit) }
    geometryTransformer.getMultiSurface().success { lod2MultiSurface = it; return Result.success(Unit) }
    geometryTransformer.getMultiCurve().success { lod2MultiCurve = it; return Result.success(Unit) }
    geometryTransformer.getPoint().success { lod0Point = it; return Result.success(Unit) }

    return Result.error(IllegalStateException("No suitable source geometry found for populating the LoD2 geometries of the abstract space."))
}

/**
 * Populates the LoD1 geometries of an [AbstractSpace] object with the source geometries of the [GeometryTransformer].
 * So only solid geometries are populated at LoD1 (since multiSurface, multiCurve are not available at this LoD) and the
 * point at LoD0 serves as accepted fallback.
 *
 * @param geometryTransformer source geometries
 * @return [Result.success] is returned, if a geometry has been populated; [Result.error], if no adequate geometry could be assigned
 */
fun AbstractSpace.populateLod1Geometries(geometryTransformer: GeometryTransformer): Result<Unit, IllegalStateException> {
    geometryTransformer.getSolid().success { lod1Solid = it; return Result.success(Unit) }
    geometryTransformer.getPoint().success { lod0Point = it; return Result.success(Unit) }

    return Result.error(IllegalStateException("No suitable source geometry found for populating the LoD1 geometries of the abstract space."))
}
