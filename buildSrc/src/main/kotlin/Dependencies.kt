/*
 * Copyright 2019-2024 Chair of Geoinformatics, Technical University of Munich
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


object DependencyVersions {
    // standard libraries
    const val kotlin = "2.1.20"
    const val coroutines = "1.10.1"
    const val arrow = "1.2.4"

    // testing libraries
    const val kotest = "5.9.1"
    const val kotestExtensionArrow = "1.4.0"
    const val mockk = "1.13.17"

    // logging libraries
    const val kotlinLogging = "7.0.6"
    const val slf4jSimple = "2.0.17"

    // object creation libraries
    const val kotlinxSerializationJson = "1.8.1"
    const val kaml = "0.74.0"
    const val jakartaActivationApi = "2.1.3"
    const val jakartaXmlBindApi = "4.0.2"
    const val jaxb = "4.0.5"

    // object mapping libraries
    const val mapstruct = "1.6.3"

    // io libraries
    const val clikt = "5.0.3"
    const val mordant = "1.2.1"
    const val commonsIO = "2.18.0"
    const val commonsCSV = "1.14.0"
    const val commonsLang = "3.17.0"
    const val commonsCompress = "1.27.1"
    const val zstdJni = "1.5.7-2"
    const val emojiJava = "5.1.1"

    // math libraries
    const val guava = "33.4.0-jre"
    const val commonsMath = "3.6.1"
    const val joml = "1.10.8"
    const val poly2tri = "0.1.2"

    // geo libraries
    const val proj4 = "1.4.0"
    const val citygml4j = "3.2.4"
}

object Dependencies {
    // standard libraries
    const val kotlinStandardLibrary = "stdlib-jdk8:${DependencyVersions.kotlin}"
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.coroutines}"
    const val arrowCore = "io.arrow-kt:arrow-core:${DependencyVersions.arrow}"
    const val arrowOptics = "io.arrow-kt:arrow-optics:${DependencyVersions.arrow}"
    const val arrowOpticsKspPlugin = "io.arrow-kt:arrow-optics-ksp-plugin:${DependencyVersions.arrow}"

    // testing libraries
    const val kotest = "io.kotest:kotest-runner-junit5:${DependencyVersions.kotest}"
    const val kotestExtensionArrow = "io.kotest.extensions:kotest-assertions-arrow:${DependencyVersions.kotestExtensionArrow}"
    const val mockk = "io.mockk:mockk:${DependencyVersions.mockk}"

    // logging libraries
    const val kotlinLogging = "io.github.oshai:kotlin-logging-jvm:${DependencyVersions.kotlinLogging}"
    const val slf4jSimple = "org.slf4j:slf4j-simple:${DependencyVersions.slf4jSimple}"

    // object creation libraries
    const val kotlinxSerializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:${DependencyVersions.kotlinxSerializationJson}"
    const val kaml = "com.charleskorn.kaml:kaml:${DependencyVersions.kaml}"
    const val jakartaActivationApi = "jakarta.activation:jakarta.activation-api:${DependencyVersions.jakartaActivationApi}"
    const val jakartaXmlBindApi = "jakarta.xml.bind:jakarta.xml.bind-api:${DependencyVersions.jakartaXmlBindApi}"
    const val sunJaxbImpl = "com.sun.xml.bind:jaxb-impl:${DependencyVersions.jaxb}"
    const val jaxbRuntime = "org.glassfish.jaxb:jaxb-runtime:${DependencyVersions.jaxb}"
    const val jaxbXjc = "org.glassfish.jaxb:jaxb-xjc:${DependencyVersions.jaxb}"

    // object mapping libraries
    const val mapstruct = "org.mapstruct:mapstruct:${DependencyVersions.mapstruct}"
    const val mapstructProcessor = "org.mapstruct:mapstruct-processor:${DependencyVersions.mapstruct}"

    // io libraries
    const val clikt = "com.github.ajalt.clikt:clikt:${DependencyVersions.clikt}"
    const val mordant = "com.github.ajalt:mordant:${DependencyVersions.mordant}"
    const val commonsIO = "commons-io:commons-io:${DependencyVersions.commonsIO}"
    const val commonsCSV = "org.apache.commons:commons-csv:${DependencyVersions.commonsCSV}"
    const val commonsLang = "org.apache.commons:commons-lang3:${DependencyVersions.commonsLang}"
    const val commonsCompress = "org.apache.commons:commons-compress:${DependencyVersions.commonsCompress}"
    const val zstdJni = "com.github.luben:zstd-jni:${DependencyVersions.zstdJni}"
    const val emojiJava = "com.vdurmont:emoji-java:${DependencyVersions.emojiJava}"

    // math libraries
    const val guava = "com.google.guava:guava:${DependencyVersions.guava}"
    const val commonsMath = "org.apache.commons:commons-math3:${DependencyVersions.commonsMath}"
    const val joml = "org.joml:joml:${DependencyVersions.joml}"
    const val poly2tri = "org.orbisgis:poly2tri-core:${DependencyVersions.poly2tri}"

    // geo libraries
    const val proj4 = "org.locationtech.proj4j:proj4j:${DependencyVersions.proj4}"
    const val proj4Epsg = "org.locationtech.proj4j:proj4j-epsg:${DependencyVersions.proj4}"
    const val citygml4jXml = "org.citygml4j:citygml4j-xml:${DependencyVersions.citygml4j}"
}
