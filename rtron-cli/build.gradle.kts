plugins {
    // application
    kotlin("jvm")
    // id(Plugins.shadowjar) version PluginVersions.shadowjar
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

    // standard libraries
    implementation(Dependencies.arrowCore)

    // io
    implementation(Dependencies.clikt)
    implementation(Dependencies.mordant)
    implementation(Dependencies.commonsLang)
}
