plugins {
}

group = "moe.mukjep.rcon"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "maven-publish")
}
