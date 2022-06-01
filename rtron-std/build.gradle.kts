plugins {
    kotlin("jvm")
    kotlin(Plugins.serialization) version PluginVersions.serialization
}

kotlinProject()

dependencies {
    // math libraries
    implementation(Dependencies.guava)
}
