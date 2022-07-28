plugins {
    kotlin("jvm")
    kotlin(Plugins.serialization) version PluginVersions.serialization
}

kotlinProject()

dependencies {
    // utility layer components
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))
    implementation(project(ProjectComponents.math))

    // single model processing layer components
    implementation(project(ProjectComponents.model))

    // object creation libraries
    implementation(Dependencies.kotlinxSerializationJson)

    // logging libraries
    implementation(Dependencies.kotlinLogging)
    implementation(Dependencies.slf4jSimple)

    // geo libraries
    implementation(Dependencies.citygml4jXml)
}
