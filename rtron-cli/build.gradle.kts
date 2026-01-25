import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm")
    id(Plugins.shadowjar) version PluginVersions.shadowjar
    kotlin(Plugins.serialization) version PluginVersions.serialization
}

kotlinProject()

dependencies {
    // utility layer
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))

    // single model processing layer
    implementation(project(ProjectComponents.readerWriter))
    implementation(project(ProjectComponents.transformer))
    implementation(project(ProjectComponents.model))

    // object creation libraries
    implementation(Dependencies.kotlinxSerializationJson)
    implementation(Dependencies.kaml)

    // io
    implementation(Dependencies.clikt)
    implementation(Dependencies.mordant)
    implementation(Dependencies.commonsLang)

    // logging libraries
    implementation(Dependencies.kotlinLogging)
    implementation(Dependencies.slf4jSimple)
}

application {
    mainClass.set("io.rtron.cli.Main")
}

tasks {
    named<ShadowJar>("shadowJar") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        mergeServiceFiles()
        archiveFileName.set("${Project.name}.${this.archiveExtension.get()}")
    }
}
