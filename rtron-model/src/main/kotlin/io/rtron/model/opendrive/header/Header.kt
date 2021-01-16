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

package io.rtron.model.opendrive.header

class Header(
    // Properties and Initializers
    var geoReference: String = "",
    // var userData: List<UserData> = listOf(),
    // var include: List<Include> = listOf(),

    var revMajor: Int = -1,
    var revMinor: Int = -1,
    var name: String = "",
    var version: Float = Float.NaN,
    var date: String = "",

    var north: Double = Double.NaN,
    var south: Double = Double.NaN,
    var east: Double = Double.NaN,
    var west: Double = Double.NaN,

    var vendor: String = ""
)
