plugins {
    kotlin("jvm")
    id(Plugins.ksp) version PluginVersions.ksp
    idea
}

kotlinProject()

dependencies {
    // utility layer components
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))
    implementation(project(ProjectComponents.math))

    ksp(Dependencies.arrowOpticsKspPlugin)

    // geo libraries
    implementation(Dependencies.citygml4jXml)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn("kspKotlin")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

// see: https://kotlinlang.org/docs/ksp-quickstart.html#make-ide-aware-of-generated-code
idea {
    module {
        // Not using += due to https://github.com/gradle/gradle/issues/8749
        sourceDirs = sourceDirs + file("build/generated/ksp/main/kotlin") // or tasks["kspKotlin"].destination
        testSourceDirs = testSourceDirs + file("build/generated/ksp/test/kotlin")
        generatedSourceDirs = generatedSourceDirs + file("build/generated/ksp/main/kotlin") + file("build/generated/ksp/test/kotlin")
    }
}
