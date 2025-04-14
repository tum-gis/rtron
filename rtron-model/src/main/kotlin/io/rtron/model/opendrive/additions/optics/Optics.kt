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

package io.rtron.model.opendrive.additions.optics

import arrow.optics.PPrism
import arrow.optics.Traversal
import io.rtron.model.opendrive.OpendriveModel
import io.rtron.model.opendrive.core.Header
import io.rtron.model.opendrive.core.geoReference
import io.rtron.model.opendrive.core.offset
import io.rtron.model.opendrive.header
import io.rtron.model.opendrive.junction
import io.rtron.model.opendrive.junction.Junction
import io.rtron.model.opendrive.junction.connection
import io.rtron.model.opendrive.lane.RoadLanes
import io.rtron.model.opendrive.lane.RoadLanesLaneSection
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenter
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionCenterLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLeft
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionLeftLane
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionRight
import io.rtron.model.opendrive.lane.RoadLanesLaneSectionRightLane
import io.rtron.model.opendrive.lane.center
import io.rtron.model.opendrive.lane.lane
import io.rtron.model.opendrive.lane.laneSection
import io.rtron.model.opendrive.lane.left
import io.rtron.model.opendrive.lane.right
import io.rtron.model.opendrive.lane.roadMark
import io.rtron.model.opendrive.objects.RoadObjects
import io.rtron.model.opendrive.objects.RoadObjectsObject
import io.rtron.model.opendrive.objects.RoadObjectsObjectOutlines
import io.rtron.model.opendrive.objects.outline
import io.rtron.model.opendrive.objects.outlines
import io.rtron.model.opendrive.objects.repeat
import io.rtron.model.opendrive.objects.roadObject
import io.rtron.model.opendrive.road
import io.rtron.model.opendrive.road.Road
import io.rtron.model.opendrive.road.elevation.RoadElevationProfile
import io.rtron.model.opendrive.road.elevation.elevation
import io.rtron.model.opendrive.road.elevationProfile
import io.rtron.model.opendrive.road.lanes
import io.rtron.model.opendrive.road.link
import io.rtron.model.opendrive.road.objects
import io.rtron.model.opendrive.road.planView
import io.rtron.model.opendrive.road.planview.RoadPlanView
import io.rtron.model.opendrive.road.planview.geometry
import io.rtron.model.opendrive.road.signals
import io.rtron.model.opendrive.signal.RoadSignals
import io.rtron.model.opendrive.signal.signal

val everyHeaderOffset = OpendriveModel.header compose Header.offset compose PPrism.some()
val everyHeaderGeoReference = OpendriveModel.header compose Header.geoReference compose PPrism.some()

// road
val everyRoad = OpendriveModel.road compose Traversal.list()
val everyRoadLink = everyRoad compose Road.link compose PPrism.some()
val everyRoadPlanView = everyRoad compose Road.planView
val everyRoadPlanViewGeometry = everyRoadPlanView compose RoadPlanView.geometry compose Traversal.list()
val everyRoadElevationProfile = everyRoad compose Road.elevationProfile
val everyRoadElevationProfileElement =
    everyRoadElevationProfile compose PPrism.some() compose RoadElevationProfile.elevation compose
        Traversal.list()

// lane section
val everyLaneSection = everyRoad compose Road.lanes compose RoadLanes.laneSection compose Traversal.list()

val everyRoadLanesLaneSectionLeftLane =
    everyLaneSection compose RoadLanesLaneSection.left compose PPrism.some() compose
        RoadLanesLaneSectionLeft.lane compose Traversal.list()
val everyRoadLanesLaneSectionRightLane =
    everyLaneSection compose RoadLanesLaneSection.right compose PPrism.some() compose
        RoadLanesLaneSectionRight.lane compose Traversal.list()
val everyRoadLanesLaneSectionCenterLane =
    everyLaneSection compose RoadLanesLaneSection.center compose
        RoadLanesLaneSectionCenter.lane

// road marks
val everyRoadLanesLaneSectionLeftLaneRoadMark =
    everyRoadLanesLaneSectionLeftLane compose
        RoadLanesLaneSectionLeftLane.roadMark compose Traversal.list()
val everyRoadLanesLaneSectionRightLaneRoadMark =
    everyRoadLanesLaneSectionRightLane compose
        RoadLanesLaneSectionRightLane.roadMark compose Traversal.list()
val everyRoadLanesLaneSectionCenterLaneRoadMark =
    everyRoadLanesLaneSectionCenterLane compose
        RoadLanesLaneSectionCenterLane.roadMark compose Traversal.list()

// junction
val everyJunction = OpendriveModel.junction compose Traversal.list()
val everyJunctionConnection = everyJunction compose Junction.connection compose Traversal.list()

// road object
val everyRoadObjectContainer = everyRoad compose Road.objects compose PPrism.some()
val everyRoadObject = everyRoadObjectContainer compose RoadObjects.roadObject compose Traversal.list()
val everyRoadObjectOutlineElement =
    everyRoadObject compose RoadObjectsObject.outlines compose PPrism.some() compose
        RoadObjectsObjectOutlines.outline compose Traversal.list()
val everyRoadObjectRepeatElement = everyRoadObject compose RoadObjectsObject.repeat compose Traversal.list()

// road signal
val everyRoadSignalContainer = everyRoad compose Road.signals compose PPrism.some()
val everyRoadSignal = everyRoadSignalContainer compose RoadSignals.signal compose Traversal.list()
