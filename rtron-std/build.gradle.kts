plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    // standard libraries
    implementation(Dependencies.arrowCore)

    // math libraries
    implementation(Dependencies.guava)
}
