/*
 * Copyright 2019-2026 Chair of Geoinformatics, Technical University of Munich
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

package io.rtron.model.citygml.code

import org.xmlobjects.gml.model.basictypes.Code

enum class AbstractBridgeClassCode(
    val code: Code,
) {
    ARCED_BRIDGE(Code("1000")),
    CABLE_STAYED_BRIDGE(Code("1010")),
    DECK_BRIDGE(Code("1020")),
    CABLE_STAYED_OVERPASS(Code("1030")),
    TRUSS_BRIDGE(Code("1040")),
    PONTOON_BRIDGE(Code("1050")),
    SUSPENSION_BRIDGE(Code("1060")),
}

enum class AbstractBridgeFunctionCode(
    val code: Code,
) {
    RAILWAY_BRIDGE(Code("1000")),
    ROADWAY_BRIDGE(Code("1010")),
    CABLE_LINK(Code("1030")),
    CANAL_BRIDGE(Code("1040")),
    AQUEDUCT(Code("1050")),
    FOOT_BRIDGE(Code("1060")),
}

enum class AbstractBridgeUsageCode(
    val code: Code,
) {
    RAILWAY_BRIDGE(Code("1000")),
    ROADWAY_BRIDGE(Code("1010")),
    CABLE_LINK(Code("1030")),
    CANAL_BRIDGE(Code("1040")),
    AQUEDUCT(Code("1050")),
    FOOT_BRIDGE(Code("1060")),
}
