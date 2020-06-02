import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id(Plugins.xjc) version PluginVersions.xjc
}

kotlinProject()

dependencies {
    implementation(project(ProjectModules.standard))
    implementation(project(ProjectModules.inputOutput))
    implementation(project(ProjectModules.model))

    xjc(Dependencies.jaxbApi)
    xjc(Dependencies.jaxbImpl)
    xjc(Dependencies.jaxbXjc)
    xjc(Dependencies.jaxbCore)
    xjc(Dependencies.javaaxActivation)

    // object mapping
    implementation(Dependencies.mapstruct)
    kapt(Dependencies.mapstructProcessor)

    implementation(Dependencies.citygml4j)
}

tasks.withType<KotlinCompile> {
    dependsOn("${ProjectModules.readerWriter}:xjcGeneration")
}

xjcGeneration {

    schemas {
        create("opendrive14") {
            schemaFile = "opendrive14/OpenDRIVE_1.4H.xsd"
            bindingFile = "src/main/schemas/xjc/opendrive14/OpenDRIVE_1.4H.xjb"
            javaPackageName = "org.asam.opendrive14"
        }

        create("opendrive15") {
            schemaFile = "opendrive15/OpenDRIVE_1.5M.xsd"
            bindingFile = "src/main/schemas/xjc/opendrive15/OpenDRIVE_1.5M.xjb"
            javaPackageName = "org.asam.opendrive15"
        }
    }

}
