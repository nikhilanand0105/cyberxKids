plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("dev.saathi.cli.MainKt")
}

dependencies {
    implementation(project(":core-apk-intelligence"))
    implementation(project(":core-sense"))
    testImplementation(kotlin("test"))
}
