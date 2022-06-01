plugins {
    kotlin("jvm")
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
    implementation(kotlin("script-runtime"))
}
