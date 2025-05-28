plugins {
    kotlin("jvm") version "2.1.10"
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":rcon4j-core"))
    api("com.squareup.okio:okio:3.10.2")
}

group = rootProject.group
version = rootProject.version

tasks.jar {
    archiveBaseName = project.name
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
