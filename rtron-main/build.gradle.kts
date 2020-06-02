plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectModules.standard))
    implementation(project(ProjectModules.inputOutput))

    implementation(project(ProjectModules.model))
    implementation(project(ProjectModules.readerWriter))
    implementation(project(ProjectModules.transformer))

    implementation(kotlin("script-runtime"))
}
