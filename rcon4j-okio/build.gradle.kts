plugins {
    alias(libs.plugins.kotlin)
    `maven-local-publish`
}

dependencies {
    api(project(":rcon4j-core"))
    api(libs.squareup.okio)
    testImplementation(kotlin("test"))
    testImplementation(project(":rcon4j-testing"))
}

description = "rcon4j(kt): Okio packet adapter and client implementation"

kotlin {
    jvmToolchain(8)
}

tasks.test {
    useJUnitPlatform()
}
