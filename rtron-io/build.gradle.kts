plugins {
    kotlin("jvm")
    kotlin(Plugins.serialization) version PluginVersions.serialization
}

kotlinProject()

dependencies {
    // user interface layer components
    implementation(project(ProjectComponents.standard))

    // object creation libraries
    implementation(Dependencies.kotlinxSerializationJson)

    // logging libraries
    implementation(Dependencies.kotlinLogging)
    implementation(Dependencies.slf4jSimple)

    // io libraries
    implementation(Dependencies.commonsIO)
    implementation(Dependencies.commonsCSV)
    implementation(Dependencies.commonsLang)
    implementation(Dependencies.commonsCompress)
    implementation(Dependencies.zstdJni)
    implementation(Dependencies.emojiJava)

    // math libraries
    implementation(Dependencies.guava)
}
