plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectModules.standard))
    testImplementation(project(ProjectModules.inputOutput))

    implementation(Dependencies.guava)
    implementation(Dependencies.commonsMath)
    implementation(Dependencies.joml)

    implementation(Dependencies.proj4)
    implementation(Dependencies.poly2tri)
}
