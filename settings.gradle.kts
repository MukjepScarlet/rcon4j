plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "rcon4j"

include("rcon4j-kotlin")
include("rcon4j-core")
