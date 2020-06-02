plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectModules.standard))
    implementation(project(ProjectModules.inputOutput))
    implementation(project(ProjectModules.math))

    implementation(Dependencies.citygml4j)
}
