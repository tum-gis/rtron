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

package io.rtron.model.roadspaces.topology.junction


/**
 * Connection identifier interface required for class delegation.
 */
interface ConnectionIdentifierInterface {
    val connectionId: String
}


/**
 * Identifier of a [Connection].
 *
 * @param connectionId id of the junction
 * @param junctionIdentifier identifier of the junction
 */
data class ConnectionIdentifier(
    override val connectionId: String,
    val junctionIdentifier: JunctionIdentifier
): ConnectionIdentifierInterface, JunctionIdentifierInterface by junctionIdentifier
