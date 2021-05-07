plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectComponents.standard))

    // logging
    implementation(Dependencies.kotlinLogging)
    implementation(Dependencies.slf4jSimple)

    implementation(Dependencies.commonsIO)
    implementation(Dependencies.commonsCSV)
    implementation(Dependencies.commonsLang)
    implementation(Dependencies.emojiJava)

    implementation(Dependencies.guava)
}
