plugins {
    kotlin("jvm")
    id(Plugins.ksp) version PluginVersions.ksp
}

kotlinProject()

dependencies {
    // utility layer components
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))
    implementation(project(ProjectComponents.math))

    ksp(Dependencies.arrowOpticsKspPlugin)

    // geo libraries
    implementation(Dependencies.citygml4j)
}

// adding generated sources of arrow optics
kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
