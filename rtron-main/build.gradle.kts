plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))

    implementation(project(ProjectComponents.model))
    implementation(project(ProjectComponents.readerWriter))
    implementation(project(ProjectComponents.transformer))

    implementation(kotlin("script-runtime"))
}
