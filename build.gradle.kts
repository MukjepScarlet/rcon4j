plugins {
}

group = "moe.mukjep.rcon"
version = "0.1.1"

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "maven-publish")
}
