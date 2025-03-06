plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":rcon4j-core"))
    implementation("io.ktor:ktor-network:3.1.1")
    testImplementation(kotlin("test"))
}

group = rootProject.group
version = rootProject.version

tasks.jar {
    archiveBaseName = "rcon4j-kotlin"
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = version
    }
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.test {
    useJUnitPlatform()
}
