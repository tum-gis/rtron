plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))
    implementation(project(ProjectComponents.math))

    implementation(Dependencies.arrowCore)
    implementation(Dependencies.citygml4j)
}
