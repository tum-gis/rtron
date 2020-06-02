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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

if (!JavaVersion.current().isJava11Compatible)
    logger.warn("This build requires Java ${JavaVersion.VERSION_11}, " +
            "but version ${JavaVersion.current()} is currently in use.")

plugins {
    base
    java
    kotlin("jvm") version DependencyVersions.kotlin
    id(Plugins.versionChecker) version PluginVersions.versionChecker
}

allprojects {
    group = Project.group
    version = Project.version

    repositories {
        maven {
            url = uri(MavenSources.ktsRunner)
        }
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "java")

    dependencies {
        implementation(kotlin(Dependencies.kotlinStandardLibrary))
        implementation(Dependencies.kotlinCoroutines)
        implementation(Dependencies.result)

        testImplementation(Dependencies.junit)
        testImplementation(Dependencies.assertj)
        testImplementation(Dependencies.mockk)
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

dependencies {
    // make the root project archives configuration depend on every sub-project
    subprojects.forEach {
        implementation(it)
    }
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
    checkForGradleUpdate = true
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}