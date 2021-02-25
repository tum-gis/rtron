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
import io.rtron.std.handleSuccess
import org.citygml4j.model.core.AbstractOccupiedSpace

/**
 * Populates the [lod] implicit geometry of an [AbstractOccupiedSpace] object with the source geometries of the [GeometryTransformer].
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Result.success] is returned, if an implicit geometry has been populated; [Result.error], if no implicit geometry could be assigned
 */
fun AbstractOccupiedSpace.populateImplicitGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Result<Unit, IllegalStateException> {
    when (lod) {
        LevelOfDetail.ZERO -> throw IllegalArgumentException("An implicit geometry can not be assigned to an AbstractOccupiedSpace at level 0.")
        LevelOfDetail.ONE -> geometryTransformer.getImplicitGeometry().success { lod1ImplicitRepresentation = it; return Result.success(Unit) }
        LevelOfDetail.TWO -> geometryTransformer.getImplicitGeometry().success { lod2ImplicitRepresentation = it; return Result.success(Unit) }
        LevelOfDetail.THREE -> geometryTransformer.getImplicitGeometry().success { lod3ImplicitRepresentation = it; return Result.success(Unit) }
    }

    return Result.error(IllegalStateException("No suitable source geometry found for populating the $lod implicit geometry of the abstract occupied space."))
}

/**
 * Populates the [lod] geometry of an [AbstractOccupiedSpace], if available. Otherwise the [lod] implicit geometry of the [GeometryTransformer] is populated.
 *
 * @param geometryTransformer source geometries
 * @param lod target level of detail
 * @return [Result.success] is returned, if a geometry or implicit geometry has been populated; [Result.error], if no geometry could be assigned
 */
fun AbstractOccupiedSpace.populateGeometryOrImplicitGeometry(geometryTransformer: GeometryTransformer, lod: LevelOfDetail): Result<Unit, IllegalStateException> {
    populateGeometry(geometryTransformer, lod).handleSuccess { return it }
    populateImplicitGeometry(geometryTransformer, lod).handleSuccess { return it }

    return Result.error(IllegalStateException("No suitable source geometry found for populating the $lod geometry or implicit geometry of the abstract occupied space."))
}
