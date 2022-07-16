/*
 * Copyright 2019-2022 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.transformer.converter.opendrive2roadspaces

import io.rtron.model.opendrive.additions.identifier.AbstractOpendriveIdentifier

sealed class Opendrive2RoadspacesTransformationException(val message: String, open val location: AbstractOpendriveIdentifier) {

    data class PlanViewGeometryException(val reason: String, override val location: AbstractOpendriveIdentifier) : Opendrive2RoadspacesTransformationException("Plan view geometry cloud not be built: $reason", location)

    data class ZeroLengthRoadMarking(override val location: AbstractOpendriveIdentifier) : Opendrive2RoadspacesTransformationException("Length of road marking is zero (or below tolerance threshold).", location)
}
