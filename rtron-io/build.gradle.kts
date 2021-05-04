plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(ProjectComponents.standard))

    implementation(kotlin("script-runtime"))
    implementation(Dependencies.kotlinReflect)
    compileOnly(Dependencies.ktsRunner) {
        exclude("org.slf4j", "slf4j-simple") // avoid multiple slf4j bindings
        exclude("ch.qos.logback", "logback-classic")
    }

    // logging
    implementation(Dependencies.log4jApi)
    implementation(Dependencies.log4jCore)
    implementation(Dependencies.slf4jSimple)

    implementation(Dependencies.commonsIO)
    implementation(Dependencies.commonsCSV)
    implementation(Dependencies.commonsLang)
    implementation(Dependencies.emojiJava)

    implementation(Dependencies.guava)
}
