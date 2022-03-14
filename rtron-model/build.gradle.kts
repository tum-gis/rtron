plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    // utility layer components
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))
    implementation(project(ProjectComponents.math))

    // standard libraries
    implementation(Dependencies.arrowCore)

    // geo libraries
    implementation(Dependencies.citygml4j)
}
