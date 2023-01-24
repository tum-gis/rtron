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

package io.rtron.model.opendrive.additions.extensions

import arrow.core.getOrElse
import arrow.core.some
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.additions.identifier.JunctionConnectionIdentifier
import io.rtron.model.opendrive.additions.identifier.JunctionIdentifier
import io.rtron.model.opendrive.additions.identifier.LaneIdentifier
import io.rtron.model.opendrive.additions.identifier.LaneRoadMarkIdentifier
import io.rtron.model.opendrive.additions.identifier.LaneSectionIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadObjectIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadObjectOutlineIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadObjectRepeatIdentifier
import io.rtron.model.opendrive.additions.identifier.RoadSignalIdentifier
import io.rtron.model.opendrive.additions.optics.everyJunction
import io.rtron.model.opendrive.additions.optics.everyLaneSection
import io.rtron.model.opendrive.additions.optics.everyRoad
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.additions.optics.everyRoadLanesLaneSectionRightLane
import io.rtron.model.opendrive.additions.optics.everyRoadObject
import io.rtron.model.opendrive.junction.connection
import io.rtron.model.opendrive.lane.laneSection
import io.rtron.model.opendrive.road.lanes

fun OpendriveModel.updateAdditionalIdentifiers() {

    everyRoad.modify(this) { currentRoad ->
        val roadId = RoadIdentifier(currentRoad.id)
        currentRoad.additionalId = roadId.some()

        currentRoad.lanes.laneSection.forEachIndexed { index, roadLanesLaneSection ->
            roadLanesLaneSection.additionalId = LaneSectionIdentifier(index, roadId).some()
        }

        currentRoad.objects.tap { roadObjects ->
            roadObjects.roadObject.forEach {
                it.additionalId = RoadObjectIdentifier(it.id, roadId).some()
            }
        }

        currentRoad.signals.tap { roadSignals ->
            roadSignals.signal.forEach {
                it.additionalId = RoadSignalIdentifier(it.id, roadId).some()
            }
        }

        currentRoad
    }

    everyLaneSection.modify(this) { currentLaneSection ->
        val currentLaneSectionId = currentLaneSection.additionalId
            .toEither { IllegalStateException("Additional ID not available.") }
            .getOrElse { throw it }

        currentLaneSection.center.lane.forEach { currentLane ->
            currentLane.additionalId = LaneIdentifier(currentLane.id, currentLaneSectionId).some()
        }

        currentLaneSection.left.tap { currentLeft ->
            currentLeft.lane.forEach { currentLane ->
                currentLane.additionalId = LaneIdentifier(currentLane.id, currentLaneSectionId).some()
            }
        }

        currentLaneSection.right.tap { currentRight ->
            currentRight.lane.forEach { currentLane ->
                currentLane.additionalId = LaneIdentifier(currentLane.id, currentLaneSectionId).some()
            }
        }

        currentLaneSection
    }

    everyRoadLanesLaneSectionCenterLane.modify(this) { currentCenterLane ->
        val currentLaneId = currentCenterLane.additionalId
            .toEither { IllegalStateException("Additional ID not available.") }
            .getOrElse { throw it }

        currentCenterLane.roadMark.forEachIndexed { index, currentRoadMark ->
            currentRoadMark.additionalId = LaneRoadMarkIdentifier(index, currentLaneId).some()
        }

        currentCenterLane
    }

    everyRoadLanesLaneSectionLeftLane.modify(this) { currentLeftLane ->
        val currentLaneId = currentLeftLane.additionalId
            .toEither { IllegalStateException("Additional ID not available.") }
            .getOrElse { throw it }

        currentLeftLane.roadMark.forEachIndexed { index, currentRoadMark ->
            currentRoadMark.additionalId = LaneRoadMarkIdentifier(index, currentLaneId).some()
        }

        currentLeftLane
    }

    everyRoadLanesLaneSectionRightLane.modify(this) { currentRightLane ->
        val currentLaneId = currentRightLane.additionalId
            .toEither { IllegalStateException("Additional ID not available.") }
            .getOrElse { throw it }

        currentRightLane.roadMark.forEachIndexed { index, currentRoadMark ->
            currentRoadMark.additionalId = LaneRoadMarkIdentifier(index, currentLaneId).some()
        }

        currentRightLane
    }

    everyJunction.modify(this) { currentJunction ->
        val junctionId = JunctionIdentifier(currentJunction.id)
        currentJunction.additionalId = junctionId.some()

        currentJunction.connection.forEach { currentJunctionConnection ->
            currentJunctionConnection.additionalId = JunctionConnectionIdentifier(currentJunctionConnection.id, junctionId).some()
        }

        currentJunction
    }

    everyRoadObject.modify(this) { currentRoadObject ->
        val currentRoadObjectId = currentRoadObject.additionalId
            .toEither { IllegalStateException("Additional ID not available.") }
            .getOrElse { throw it }

        currentRoadObject.outlines.tap { currentRoadObjectOutlines ->

            currentRoadObjectOutlines.outline.forEach {
                it.additionalId = RoadObjectOutlineIdentifier(it.id.getOrElse { Int.MIN_VALUE }, currentRoadObjectId).some()
            }
        }

        currentRoadObject.repeat.forEachIndexed { index, currenRepeat ->
            currenRepeat.additionalId = RoadObjectRepeatIdentifier(index, currentRoadObjectId).some()
        }

        currentRoadObject
    }
}
