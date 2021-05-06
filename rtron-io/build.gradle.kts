plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectComponents.standard))

    implementation(kotlin("script-runtime"))
    implementation(Dependencies.kotlinReflect)

    // logging
    implementation(Dependencies.log4jApi)
    implementation(Dependencies.log4jCore)

    implementation(Dependencies.commonsIO)
    implementation(Dependencies.commonsCSV)
    implementation(Dependencies.commonsLang)
    implementation(Dependencies.emojiJava)

    implementation(Dependencies.guava)
}
