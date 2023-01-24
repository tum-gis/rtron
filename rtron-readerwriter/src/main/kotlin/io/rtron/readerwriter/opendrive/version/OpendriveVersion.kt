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

package io.rtron.readerwriter.opendrive.version

import arrow.core.Either
import io.rtron.std.getValueEither

/**
 * Representation of a specific OpenDRIVE version.
 *
 * For more details, see:
 * ASAM: https://www.asam.net/standards/detail/opendrive
 * Wikipedia: https://en.wikipedia.org/wiki/OpenDRIVE_(specification)
 */
enum class OpendriveVersion(val rev: Pair<Int, Int>) {

    /**
     * OpenDRIVE version 0.7 released 2005 by VIRES Simulationstechnologie GmbH.
     */
    V_0_7(Pair(0, 7)),

    /**
     * OpenDRIVE version 1.1 Rev.D released on the 11 Apr 2008 by VIRES Simulationstechnologie GmbH.
     */
    V_1_1(Pair(1, 1)),

    /**
     * OpenDRIVE version 1.2 Rev.A released on the 06 Jan 2008 by VIRES Simulationstechnologie GmbH.
     */
    V_1_2(Pair(1, 2)),

    /**
     * OpenDRIVE version 1.3 Rev.C released on the 07 Aug 2010 by VIRES Simulationstechnologie GmbH.
     */
    V_1_3(Pair(1, 3)),

    /**
     * OpenDRIVE version 1.4 Rev.H released on the 04 Nov 2015 by VIRES Simulationstechnologie GmbH.
     */
    V_1_4(Pair(1, 4)),

    /**
     * OpenDRIVE version 1.5 released on the 17 Feb 2019 by ASAM e.V.
     */
    V_1_5(Pair(1, 5)),

    /**
     * OpenDRIVE version 1.6.1 released on the 04 March 2021 by ASAM e.V.
     */
    V_1_6(Pair(1, 6)),

    /**
     * OpenDRIVE version 1.7.0 released on the 03 Aug 2021 by ASAM e.V.
     */
    V_1_7(Pair(1, 7));

    // Methods
    override fun toString() = "v.${rev.first}.${rev.second}"

    companion object {
        private val map = OpendriveVersion.values().associateBy(OpendriveVersion::rev)

        fun ofRevision(revMajor: Int, revMinor: Int): Either<UnknownOpendriveVersion, OpendriveVersion> =
            map.getValueEither(Pair(revMajor, revMinor)).mapLeft { UnknownOpendriveVersion(it.message) }

        data class UnknownOpendriveVersion(val message: String) // : OpendriveReaderException("Version of OpenDRIVE dataset not deducible: $reason")
    }
}
