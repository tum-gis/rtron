plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectComponents.standard))
    testImplementation(project(ProjectComponents.inputOutput))

    implementation(Dependencies.arrowCore)

    implementation(Dependencies.guava)
    implementation(Dependencies.commonsMath)
    implementation(Dependencies.joml)

    implementation(Dependencies.proj4)
    implementation(Dependencies.poly2tri)
}
