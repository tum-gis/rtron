plugins {
    // application
    kotlin("jvm")
    // id(Plugins.shadowjar) version PluginVersions.shadowjar
}

kotlinProject()

dependencies {
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))

    implementation(project(ProjectComponents.readerWriter))
    implementation(project(ProjectComponents.transformer))
    implementation(project(ProjectComponents.model))

    implementation(project(ProjectComponents.main))

    implementation(Dependencies.commonsLang)
    implementation(Dependencies.clikt)
    implementation(Dependencies.mordant)
}
