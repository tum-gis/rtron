plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))
    implementation(project(ProjectComponents.math))

    implementation(project(ProjectComponents.model))

    implementation(Dependencies.citygml4j)
}
