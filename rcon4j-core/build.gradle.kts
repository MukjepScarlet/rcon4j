plugins {
    id("java")
}

dependencies {
    compileOnly("org.jetbrains:annotations:25.0.0")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.jar {
    archiveBaseName = "rcon4j-core"
    manifest {
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = version
    }
}

tasks.test {
    useJUnitPlatform()
}
