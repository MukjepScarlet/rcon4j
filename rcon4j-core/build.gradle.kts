plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test"))
}

group = rootProject.group
version = rootProject.version

tasks.jar {
    archiveBaseName = "rcon4j-core"
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
