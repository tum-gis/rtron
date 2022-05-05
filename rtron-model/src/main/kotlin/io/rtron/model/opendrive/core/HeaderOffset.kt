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

import io.rtron.model.opendrive.additions.exceptions.OpendriveException

data class HeaderOffset(
    var hdg: Double = 0.0,
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
) : OpendriveElement() {

    // Methods
    fun healMinorViolations(): List<OpendriveException> {
        val healedViolations = mutableListOf<OpendriveException>()

        if (!x.isFinite()) {
            healedViolations += OpendriveException.UnexpectedValue("x", x.toString(), "Value should be finite.")
            x = 0.0
        }

        if (!y.isFinite()) {
            healedViolations += OpendriveException.UnexpectedValue("y", y.toString(), "Value should be finite.")
            y = 0.0
        }

        if (!z.isFinite()) {
            healedViolations += OpendriveException.UnexpectedValue("z", z.toString(), "Value should be finite.")
            z = 0.0
        }

        if (!hdg.isFinite()) {
            healedViolations += OpendriveException.UnexpectedValue("hdg", hdg.toString(), "Value should be finite.")
            hdg = 0.0
        }

        return healedViolations
    }
}
