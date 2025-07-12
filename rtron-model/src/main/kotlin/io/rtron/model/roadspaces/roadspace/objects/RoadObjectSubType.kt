/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.roadspaces.roadspace.objects

import arrow.core.Option
import arrow.core.toOption

/**
 * Represents a subtype of a road object, providing additional classification and context.
 *
 * @property identifier A unique string that identifies the specific road object subtype.
 *
 * @see <a href="https://publications.pages.asam.net/standards/ASAM_OpenDRIVE/ASAM_OpenDRIVE_Specification/latest/specification/13_objects/13_14_object_examples.html">ASAM OpenDRIVE Object Examples</a>
 */
sealed interface RoadObjectSubType {
    val identifier: String

    companion object {
        inline fun <reified T> fromIdentifier(identifier: String): Option<T> where T : RoadObjectSubType, T : Enum<T> =
            enumValues<T>().find { it.identifier == identifier }.toOption()
    }
}

enum class RoadObjectBarrierSubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** common hedge made out of vegetation and bushes without gaps */
    HEDGE("hedge"),

    /** metal guard rail along the side of the road (without the vertical poles) */
    GUARD_RAIL("guardRail"),

    /** lower wall mostly made out of concrete to separate driving lanes */
    JERSEY_BARRIER("jerseyBarrier"),

    /** higher wall out of concrete, bricks, stones ... */
    WALL("wall"),

    /** any kind of railing along the roadside */
    RAILING("railing"),

    /** metal or wooden fence */
    FENCE("fence"),

    /** higher wall for noise protection */
    NOISE_PROTECTIONS("noiseProtections"),
}

enum class RoadObjectBuildingSubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** regular building like a house or office */
    BUILDING("building"),

    /** bus stop with little roof and sign */
    BUS_STOP("busStop"),

    /** small building with a barrier to collect tolls or charges */
    TOLL_BOOTH("tollBooth"),
}

enum class RoadObjectCrosswalkSubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** pedestrian crosswalk without zebra markings */
    PEDESTRIAN("pedestrian"),

    /** bicycle crossing, in Germany normally with red paint */
    BICYCLE("bicycle"),

    /** zebra crossing */
    ZEBRA("zebra"),

    /** invisible crosswalk */
    VIRTUAL("virtual"),
}

enum class RoadObjectGantrySubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** has poles on either side of lanes and an overhead construction between them */
    GANTRY("gantry"),

    /** has a pole on one side of the road and an overhead construction attached to it */
    GANTRY_HALF("gantryHalf"),
}

enum class RoadObjectObstacleSubType(
    override val identifier: String,
) : RoadObjectSubType {
    ADVERTISING_COLUMN("advertisingColumn"),
    ART("art"),
    SEATING("seating"),
    PICK_NICK("picknick"),
    BOX("box"),
    PHONE_BOOTH("phonebooth"),
    CHARGING_STATION("chargingStation"),

    /** for example, electrical, communication */
    DISTRIBUTION_BOX("distributionBox"),
    CRASH_BOX("crashBox"),
    DUMPSTER("dumpster"),
    DUST_BIN("dustbin"),
    FOUNTAIN("fountain"),
    GRIT_CONTAINER("gritContainer"),
    HYDRANT("hydrant"),
    PARKING_METER("parkingMeter"),

    /** for example, bridge pillars */
    PILLAR("pillar"),
    PLANT_POT("plantPot"),
    POST_BOX("postBox"),

    /** for example, bicycle stand, handrail */
    RAILING("railing"),
    ROCK("rock"),
    ROAD_BLOCKAGE("roadBlockage"),
    WALL("wall"),
    FENCE("fence"),
}

enum class RoadObjectParkingSpaceSubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** typically outdoors, no limit to the top */
    OPEN_SPACE("openSpace"),

    /** typically indoors, limit to the top for example, inside a building */
    CLOSED_SPACE("closedSpace"),
}

enum class RoadObjectPoleSubType(
    override val identifier: String,
) : RoadObjectSubType {
    EMERGENCY_CALL_BOX("emergencyCallBox"),
    PERMANENT_DELINEATOR("permanentDelineator"),
    BOLLARD("bollard"),

    /** pole for Traffic Signs */
    TRAFFIC_SIGN("trafficSign"),

    /** pole for trafficLight and trafficSign objects */
    TRAFFIC_LIGHT("trafficLight"),

    /** has power cables attached */
    POWER_POLE("powerPole"),

    /** has a light source. Might also have trafficSigns or trafficLights attached to it */
    STREET_LAMP("streetLamp"),
    WIND_TURBINE("windTurbine"),
}

enum class RoadObjectRoadMarkSubType(
    override val identifier: String,
) : RoadObjectSubType {
    ARROW_LEFT("arrowLeft"),
    ARROW_LEFT_LEFT("arrowLeftLeft"),
    ARROW_LEFT_RIGHT("arrowLeftRight"),
    ARROW_RIGHT("arrowRight"),
    ARROW_RIGHT_RIGHT("arrowRightRight"),
    ARROW_RIGHT_LEFT("arrowRightLeft"),
    ARROW_STRAIGHT("arrowStraight"),
    ARROW_STRAIGHT_LEFT("arrowStraightLeft"),
    ARROW_STRAIGHT_RIGHT("arrowStraightRight"),
    ARROW_STRAIGHT_LEFT_RIGHT("arrowStraightLeftRight"),
    ARROW_MERGE_LEFT("arrowMergeLeft"),
    ARROW_MERGE_RIGHT("arrowMergeRight"),

    /** these are referenced by a signal */
    SIGNAL_LINES("signalLines"),

    /** for example, YIELD or 50, might be referenced by a signal */
    TEXT("text"),

    /** for example, Wheelchair or bicycle */
    SYMBOL("symbol"),
    PAINT("paint"),

    /** for example, restricted area, keep clear area */
    AREA("area"),
}

enum class RoadObjectRoadSurfaceSubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** mostly metal cover to access sewerage tunnels */
    MANHOLE("manhole"),

    /** road damage */
    POTHOLE("pothole"),

    /** road damage that has been fixed */
    PATCH("patch"),

    /** mostly raised surface to prevent higher speeds **/
    SPEED_BUMP("speedbump"),

    /** water drainage */
    DRAIN_GUTTER("drainGutter"),
}

enum class RoadObjectTrafficIslandSubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** typical traffic island with some curbstone, road mark */
    ISLAND("island"),
}

enum class RoadObjectTreeSubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** needle tree */
    NEEDLE("needle"),

    /** leaf tree */
    LEAF("leaf"),

    /** palm tree */
    PALM("palm"),
}

enum class RoadObjectVegetationSubType(
    override val identifier: String,
) : RoadObjectSubType {
    /** a single bush */
    BUSH("bush"),

    /** an area that is a forest */
    FOREST("forest"),

    /** a single hedge */
    HEDGE("hedge"),
}
