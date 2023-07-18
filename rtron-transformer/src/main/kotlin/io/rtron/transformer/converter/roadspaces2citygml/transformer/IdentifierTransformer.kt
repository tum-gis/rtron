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

package io.rtron.transformer.converter.roadspaces2citygml.transformer

import io.rtron.model.roadspaces.identifier.JunctionIdentifier
import io.rtron.model.roadspaces.identifier.LaneIdentifier
import io.rtron.model.roadspaces.identifier.LateralLaneRangeIdentifier
import io.rtron.model.roadspaces.identifier.LongitudinalLaneRangeIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceIdentifier
import io.rtron.model.roadspaces.identifier.RoadspaceObjectIdentifier
import java.util.*

fun RoadspaceObjectIdentifier.deriveGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, this.hashKey)

fun RoadspaceObjectIdentifier.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "TrafficSpaceOrAuxiliaryTrafficSpace_${this.hashKey}")

fun RoadspaceObjectIdentifier.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "TrafficAreaOrAuxiliaryTrafficArea_${this.hashKey}")

fun RoadspaceObjectIdentifier.deriveLod2RoofGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "Lod2RoofSurface_${this.hashKey}")

fun RoadspaceObjectIdentifier.deriveLod2GroundGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "Lod2GroundSurface_${this.hashKey}")

fun RoadspaceObjectIdentifier.deriveLod2WallGmlIdentifier(prefix: String, wallIndex: Int): String =
    generateGmlIdentifier(prefix, "Lod2WallSurface_${wallIndex}_${this.hashKey}")

fun JunctionIdentifier.deriveIntersectionGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "Intersection_${this.hashKey}")

fun LaneIdentifier.deriveTrafficSpaceOrAuxiliaryTrafficSpaceGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "TrafficSpaceOrAuxiliaryTrafficSpace_${this.hashKey}")

fun LaneIdentifier.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "TrafficAreaOrAuxiliaryTrafficArea_${this.hashKey}")

fun LaneIdentifier.deriveRoadMarkingGmlIdentifier(prefix: String, roadMarkingIndex: Int): String =
    generateGmlIdentifier(prefix, "RoadMarking_${roadMarkingIndex}_${this.hashKey}")

fun LateralLaneRangeIdentifier.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "LateralFillerSurface_${this.hashKey}")

fun LongitudinalLaneRangeIdentifier.deriveTrafficAreaOrAuxiliaryTrafficAreaGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "LongitudinalFillerSurface_${this.hashKey}")

fun RoadspaceIdentifier.deriveSectionGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "Section_${this.hashKey}")

fun RoadspaceIdentifier.deriveRoadReferenceLineGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "RoadReferenceLine_${this.hashKey}")
fun LaneIdentifier.deriveRoadCenterLaneLineGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "RoadCenterLaneLine_${this.hashKey}")

fun LaneIdentifier.deriveLaneCenterLineGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "LaneCenterLine_${this.hashKey}")

fun LaneIdentifier.deriveLeftLaneBoundaryGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "LeftLaneBoundary_${this.hashKey}")

fun LaneIdentifier.deriveRightLaneBoundaryGmlIdentifier(prefix: String): String =
    generateGmlIdentifier(prefix, "RightLaneBoundary_${this.hashKey}")

fun generateRoadIdentifier(roadName: String, prefix: String): String =
    generateGmlIdentifier(prefix, "Road_$roadName")

private fun generateGmlIdentifier(prefix: String, hashKey: String): String {
    val uuid = UUID.nameUUIDFromBytes(hashKey.toByteArray()).toString()
    return prefix + uuid
}

fun generateRandomUUID(prefix: String): String = prefix + UUID.randomUUID().toString()
