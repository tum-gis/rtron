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

import org.gradle.api.Project

import org.gradle.kotlin.dsl.*

/**
 * Configures the current project as a Kotlin project by adding the Kotlin `stdlib` as a dependency.
 */
fun Project.kotlinProject() {
    dependencies {
        "implementation"(kotlin(Dependencies.kotlinStandardLibrary))
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = """^[0-9,.v-]+(-r)?$""".toRegex()
    return !stableKeyword && !(regex.containsMatchIn(version))
}
