plugins {
    alias(libs.plugins.kotlin)
    `maven-local-publish`
}

dependencies {
    api(project(":rcon4j-core"))
    api(libs.ktor.network)
}

description = "rcon4j(kt): Ktor ByteChannel adapter for packets and coroutine-based client implementation"

kotlin {
    jvmToolchain(8)
}


tasks.test {
    useJUnitPlatform()
}
