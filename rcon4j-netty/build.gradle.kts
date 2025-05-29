plugins {
    `maven-local-publish`
}

dependencies {
    api(project(":rcon4j-core"))
    api(libs.bundles.netty)
    compileOnly(libs.jetbrains.annotations)
}

description = "rcon4j: Netty ByteBuf adapter for packets and packet codec"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.test {
    useJUnitPlatform()
}
