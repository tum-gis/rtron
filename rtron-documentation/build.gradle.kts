plugins {
    kotlin("jvm")
    id(Plugins.orchid) version PluginVersions.orchid
}

kotlinProject()

repositories {
    jcenter()
}

dependencies {
    // documentation libraries
    orchidImplementation("io.github.javaeden.orchid:OrchidCore:${DependencyVersions.orchid}")
    orchidImplementation("io.github.javaeden.orchid:OrchidCopper:${DependencyVersions.orchid}")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidDocs:${DependencyVersions.orchid}")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidSearch:${DependencyVersions.orchid}")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidPluginDocs:${DependencyVersions.orchid}")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidKotlindoc:${DependencyVersions.orchid}")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidSyntaxHighlighter:${DependencyVersions.orchid}")
}

orchid {
    environment = if (findProperty("env") == "prod") { "prod" } else { "debug" }
    args = listOf("--experimentalSourceDoc")
}
