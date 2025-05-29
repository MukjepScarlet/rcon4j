plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "rcon4j"

include("rcon4j-core")
include("rcon4j-okio")
include("rcon4j-netty")
include("rcon4j-ktor")

include("rcon4j-testing")
