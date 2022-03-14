plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    // user interface layer components
    implementation(project(ProjectComponents.standard))

    // standard libraries
    implementation(Dependencies.arrowCore)

    // logging libraries
    implementation(Dependencies.kotlinLogging)
    implementation(Dependencies.slf4jSimple)

    // io libraries
    implementation(Dependencies.commonsIO)
    implementation(Dependencies.commonsCSV)
    implementation(Dependencies.commonsLang)
    implementation(Dependencies.emojiJava)

    // math libraries
    implementation(Dependencies.guava)
}
