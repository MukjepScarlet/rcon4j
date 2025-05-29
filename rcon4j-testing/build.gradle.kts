plugins {
    alias(libs.plugins.kotlin)
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":rcon4j-netty"))
}

kotlin {
    jvmToolchain(8)
}
