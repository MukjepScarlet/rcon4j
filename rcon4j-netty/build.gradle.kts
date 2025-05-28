plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":rcon4j-core"))
    api("io.netty:netty-transport:4.1.115.Final")
    api("io.netty:netty-buffer:4.1.115.Final")
    api("io.netty:netty-common:4.1.115.Final")
    api("io.netty:netty-resolver:4.1.115.Final")
    api("io.netty:netty-handler:4.1.115.Final")
    compileOnly("org.jetbrains:annotations:24.0.0")
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

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
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
