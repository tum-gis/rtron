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

package io.rtron.main.system

/**
 * Representation of the running Java version.
 *
 * @param major major version component
 * @param minor minor version component
 * @param patch patch version component
 */
data class JavaVersion(val major: Int, val minor: Int, val patch: Int = 0) {

    // Properties and Initializers
    constructor(versionComponents: List<Int>) : this(versionComponents[0], versionComponents[1], versionComponents[2]) {
        require(versionComponents.size == 3) { "Version component list must contain exactly 3 entries." }
    }

    // Methods
    fun isAtLeast(major: Int, minor: Int): Boolean =
        this.major > major || (this.major == major && this.minor >= minor)

    fun isAtLeast(major: Int, minor: Int, patch: Int): Boolean =
        this.major > major || (this.major == major && (this.minor > minor || this.minor == minor && this.patch >= patch))

    // Conversions
    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        val CURRENT: JavaVersion = JavaVersionCurrentValue.get()
    }
}

object JavaVersionCurrentValue {

    fun get(): JavaVersion {
        val version = System.getProperty("java.version")
        check(version.isNotBlank()) { "No version found for java." }

        val versionComponents = version.split(".", "_")
        check(versionComponents.size == 3 || versionComponents.size == 4) { "Unknown format of java version." }

        val normalizedVersionComponents = versionComponents.map { it.toInt() }.takeLast(3)
        return JavaVersion(normalizedVersionComponents)
    }
}
