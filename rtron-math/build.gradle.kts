plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    // utility layer components
    implementation(project(ProjectComponents.standard))
    testImplementation(project(ProjectComponents.inputOutput))

    // math libraries
    implementation(Dependencies.guava)
    implementation(Dependencies.commonsMath)
    implementation(Dependencies.joml)

    // geo libraries
    implementation(Dependencies.proj4)
    implementation(Dependencies.poly2tri)

    // testing
    testImplementation(Dependencies.commonsCSV)
    testImplementation(Dependencies.kotlinLogging)
}
