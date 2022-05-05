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

package io.rtron.model.opendrive.core

import arrow.core.None
import arrow.core.Option
import arrow.core.Validated
import arrow.core.invalid
import arrow.core.valid
import io.rtron.model.opendrive.additions.exceptions.OpendriveException
data class Header(
    var geoReference: Option<HeaderGeoReference> = None,
    var offset: Option<HeaderOffset> = None,

    var date: Option<String> = None,
    var east: Option<Double> = None,
    var name: Option<String> = None,
    var north: Option<Double> = None,
    var revMajor: Int = -1,
    var revMinor: Int = -1,
    var south: Option<Double> = None,
    var vendor: Option<String> = None,
    var version: Option<String> = None,
    var west: Option<Double> = None
) : OpendriveElement() {

    // Properties and Initializers
    val revMajorValidated: Validated<OpendriveException.UnexpectedValue, Int>
        get() = if (revMajor > 0) revMajor.valid() else OpendriveException.UnexpectedValue("revMajor", revMajor.toString()).invalid()

    val revMinorValidated: Validated<OpendriveException.UnexpectedValue, Int>
        get() = if (revMinor > 0) revMinor.valid() else OpendriveException.UnexpectedValue("revMinor", revMinor.toString()).invalid()

    // Methods
    fun getSevereViolations(): List<OpendriveException> =
        revMajorValidated.fold({ listOf(it) }, { emptyList() }) +
            revMinorValidated.fold({ listOf(it) }, { emptyList() })

    fun healMinorViolations(): List<OpendriveException> {
        val healedViolations = mutableListOf<OpendriveException>()

        if (name.exists { it.isEmpty() }) {
            healedViolations += OpendriveException.EmptyValueForOptionalAttribute("name")
            name = None
        }

        if (date.exists { it.isEmpty() }) {
            healedViolations += OpendriveException.EmptyValueForOptionalAttribute("date")
            date = None
        }

        if (vendor.exists { it.isEmpty() }) {
            healedViolations += OpendriveException.EmptyValueForOptionalAttribute("vendor")
            vendor = None
        }

        if (north.exists { !it.isFinite() }) {
            healedViolations += OpendriveException.EmptyValueForOptionalAttribute("north")
            north = None
        }

        return healedViolations
    }
}
