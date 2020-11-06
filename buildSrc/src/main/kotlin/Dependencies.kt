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


object DependencyVersions {

    // standard libraries
    const val kotlin = "1.4.10"
    const val coroutines = "1.4.1"
    const val result = "3.1.0"
    const val javaaxActivation = "1.1.1"
    const val ktsRunner = "0.0.8"

    // testing
    const val junit = "5.7.0"
    const val assertj = "3.18.0"
    const val mockk = "1.10.2"

    // logging
    const val log4j = "2.13.3"
    const val slf4jSimple = "1.7.30"

    // documentation
    const val orchid = "0.21.1"

    // object creation
    const val jaxb = "2.3.3"
    const val jaxbApi = "2.3.1"
    const val jaxbCore = "2.3.0.1"

    // object mapping
    const val mapstruct = "1.4.1.Final"

    // io
    const val clikt = "3.0.1"
    const val mordant = "1.2.1"
    const val commonsIO = "2.8.0"
    const val commonsCSV = "1.8"
    const val commonsLang = "3.11"
    const val emojiJava = "5.1.1"

    // math
    const val guava = "29.0-jre"
    const val commonsMath = "3.6.1"
    const val joml = "1.9.25"
    const val poly2tri = "0.1.2"
    //const val tinfourCore = "2.1.4"

    // geo
    const val proj4 = "1.1.1"
    const val citygml4j = "2.11.1"
}

object Dependencies {
    // standard libraries
    const val kotlinStandardLibrary = "stdlib-jdk8"
    const val kotlinCoroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${DependencyVersions.coroutines}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${DependencyVersions.kotlin}"
    const val result = "com.github.kittinunf.result:result:${DependencyVersions.result}"
    const val javaaxActivation = "javax.activation:activation:${DependencyVersions.javaaxActivation}"
    const val ktsRunner = "de.swirtz:ktsRunner:${DependencyVersions.ktsRunner}"

    // testing
    const val junit = "org.junit.jupiter:junit-jupiter:${DependencyVersions.junit}"
    const val assertj = "org.assertj:assertj-core:${DependencyVersions.assertj}"
    const val mockk = "io.mockk:mockk:${DependencyVersions.mockk}"

    // logging
    const val log4jApi = "org.apache.logging.log4j:log4j-api:${DependencyVersions.log4j}"
    const val log4jCore = "org.apache.logging.log4j:log4j-core:${DependencyVersions.log4j}"
    const val slf4jSimple = "org.slf4j:slf4j-simple:${DependencyVersions.slf4jSimple}"

    // object creation
    const val jaxbApi = "javax.xml.bind:jaxb-api:${DependencyVersions.jaxbApi}"
    const val jaxbImpl = "com.sun.xml.bind:jaxb-impl:${DependencyVersions.jaxb}"
    const val jaxbXjc = "com.sun.xml.bind:jaxb-xjc:${DependencyVersions.jaxb}"
    const val jaxbCore = "com.sun.xml.bind:jaxb-core:${DependencyVersions.jaxbCore}"

    // object mapping
    const val mapstruct = "org.mapstruct:mapstruct:${DependencyVersions.mapstruct}"
    const val mapstructProcessor = "org.mapstruct:mapstruct-processor:${DependencyVersions.mapstruct}"

    // io
    const val clikt = "com.github.ajalt.clikt:clikt:${DependencyVersions.clikt}"
    const val mordant = "com.github.ajalt:mordant:${DependencyVersions.mordant}"
    const val commonsIO = "commons-io:commons-io:${DependencyVersions.commonsIO}"
    const val commonsCSV = "org.apache.commons:commons-csv:${DependencyVersions.commonsCSV}"
    const val commonsLang = "org.apache.commons:commons-lang3:${DependencyVersions.commonsLang}"
    const val emojiJava = "com.vdurmont:emoji-java:${DependencyVersions.emojiJava}"

    // math
    const val guava = "com.google.guava:guava:${DependencyVersions.guava}"
    const val commonsMath = "org.apache.commons:commons-math3:${DependencyVersions.commonsMath}"
    const val joml = "org.joml:joml:${DependencyVersions.joml}"
    const val poly2tri = "org.orbisgis:poly2tri-core:${DependencyVersions.poly2tri}"

    // geo
    const val proj4 = "org.locationtech.proj4j:proj4j:${DependencyVersions.proj4}"
    const val citygml4j = "org.citygml4j:citygml4j:${DependencyVersions.citygml4j}"
}
