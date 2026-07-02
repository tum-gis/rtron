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

enum class AbstractTunnelClassCode(
    val code: Code,
) {
    TRAFFIC(Code("1000")),
    SUPPLY(Code("1010")),
    HISTORICAL(Code("1020")),
    OTHERS(Code("1030")),
}

enum class AbstractTunnelFunctionCode(
    val code: Code,
) {
    RAILWAY_TUNNEL(Code("1000")),
    ROADWAY_TUNNEL(Code("1010")),
    CANAL_TUNNEL(Code("1020")),
    PEDESTRIAN_TUNNEL(Code("1030")),
}

enum class AbstractTunnelUsageCode(
    val code: Code,
) {
    RAILWAY_TUNNEL(Code("1000")),
    ROADWAY_TUNNEL(Code("1010")),
    CANAL_TUNNEL(Code("1020")),
    PEDESTRIAN_TUNNEL(Code("1030")),
}
