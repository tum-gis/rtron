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

    // batch processing layer
    implementation(project(ProjectComponents.main))

    // object creation libraries
    implementation(Dependencies.kotlinxSerializationJson)
    implementation(Dependencies.kaml)

    // io
    implementation(Dependencies.clikt)
    implementation(Dependencies.mordant)
    implementation(Dependencies.commonsLang)
}

application {
    mainClass.set("io.rtron.cli.Main")
}

tasks {
    named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        archiveFileName.set("${Project.name}-${Project.version}.${this.archiveExtension.get()}")
    }
}
