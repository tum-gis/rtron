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
    implementation(project(ProjectModules.standard))
    implementation(project(ProjectModules.inputOutput))

    implementation(project(ProjectModules.main))

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
