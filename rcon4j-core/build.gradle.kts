plugins {
    alias(libs.plugins.kotlin)
    `maven-local-publish`
}

dependencies {
    compileOnly(libs.jetbrains.annotations)
    testImplementation(kotlin("test"))
    testImplementation(project(":rcon4j-testing"))
}

description = "rcon4j: RCON protocol implementation in Java 8, including packets and client API"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    jvmToolchain(8)
}

tasks.test {
    useJUnitPlatform()
}
