import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    kotlin("jvm")
    id(Plugins.shadowjar) version PluginVersions.shadowjar
}

kotlinProject()

application {
    mainClassName = "io.rtron.cli.Main"
}

dependencies {
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))

    implementation(project(ProjectComponents.main))

    implementation(Dependencies.commonsLang)
    implementation(Dependencies.clikt)
    implementation(Dependencies.mordant)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = application.mainClassName
        attributes["Multi-Release"] = true
    }

    from(configurations.runtime.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set(Project.name)
        archiveClassifier.set("")
        mergeServiceFiles()
    }
}
