import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask

plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin(Plugins.serialization) version PluginVersions.serialization
    id(Plugins.xjc) version PluginVersions.xjc
}

kotlinProject()

dependencies {
    // utility layer components
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))
    implementation(project(ProjectComponents.model))

    // object creation libraries
    implementation(Dependencies.kotlinxSerializationJson)
    implementation(Dependencies.jakartaXmlBindApi)
    implementation(Dependencies.sunJaxbImpl)
    xjc(Dependencies.jakartaXmlBindApi)
    xjc(Dependencies.jaxbRuntime)
    xjc(Dependencies.jaxbXjc)
    xjc(Dependencies.jakartaActivationApi)

    // object mapping libraries
    implementation(Dependencies.mapstruct)
    kapt(Dependencies.mapstructProcessor)

    // logging libraries
    implementation(Dependencies.kotlinLogging)
    implementation(Dependencies.slf4jSimple)

    // geo libraries
    implementation(Dependencies.citygml4jXml)
}

xjcGeneration {
    defaultAdditionalXjcOptions = mapOf("encoding" to "UTF-8")

    schemas {
        create("opendrive11") {
            schemaDir = "opendrive11/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive11.xjb"
            javaPackageName = "org.asam.opendrive11"
        }

        create("opendrive12") {
            schemaDir = "opendrive12/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive12.xjb"
            javaPackageName = "org.asam.opendrive12"
        }

        create("opendrive13") {
            schemaDir = "opendrive13/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive13.xjb"
            javaPackageName = "org.asam.opendrive13"
        }

        create("opendrive14") {
            schemaDir = "opendrive14/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive14.xjb"
            javaPackageName = "org.asam.opendrive14"
        }

        create("opendrive15") {
            schemaDir = "opendrive15/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive15.xjb"
            javaPackageName = "org.asam.opendrive15"
        }

        create("opendrive16") {
            schemaDir = "opendrive16/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive16.xjb"
            javaPackageName = "org.asam.opendrive16"
        }

        create("opendrive17") {
            schemaDir = "opendrive17/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive17.xjb"
            javaPackageName = "org.asam.opendrive17"
        }

        /*create("opendrive18") {
            schemaDir = "opendrive18/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive18.xjb"
            javaPackageName = "org.asam.opendrive18"
        }*/
    }
}

tasks.withType<KotlinCompile> {
    dependsOn("${ProjectComponents.readerWriter}:xjcGeneration")
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("${ProjectComponents.readerWriter}:xjcGeneration")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named<org.gradle.jvm.tasks.Jar>("kotlinSourcesJar") {
    dependsOn("${ProjectComponents.readerWriter}:xjcGeneration")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<DokkaTaskPartial> {
    dependsOn("${ProjectComponents.readerWriter}:kaptKotlin")
    dependsOn("${ProjectComponents.readerWriter}:xjcGeneration")
}

tasks.withType<KtLintCheckTask> {
    dependsOn("${ProjectComponents.readerWriter}:xjcGeneration")
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        // disabled due to auto generated code throwing warnings/errors
        addBooleanOption("Xdoclint:none", true)
    }
}
