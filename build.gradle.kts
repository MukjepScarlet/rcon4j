plugins {
    kotlin("jvm") version "2.1.10"
}

group = "moe.mukjep.rcon"
version = "0.2.0"

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "maven-publish")
}
