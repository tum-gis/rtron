plugins {
    kotlin("jvm")
    kotlin(Plugins.serialization) version PluginVersions.serialization
}

kotlinProject()

dependencies {
    // utility layer components
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))

    // single model processing layer components
    implementation(project(ProjectComponents.model))
    implementation(project(ProjectComponents.readerWriter))
    implementation(project(ProjectComponents.transformer))

    // object creation libraries
    implementation(Dependencies.kotlinxSerializationJson)

    // logging libraries
    implementation(Dependencies.kotlinLogging)
    implementation(Dependencies.slf4jSimple)
}
