import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*

class MavenLocalPublishPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        // Apply required plugins
        pluginManager.apply("maven-publish")
        pluginManager.apply("java-library")

        val sourcesJar by tasks.registering(Jar::class) {
            archiveClassifier.set("sources")
            from(project.extensions.getByType<org.gradle.api.tasks.SourceSetContainer>()["main"].allSource)
        }

        val javadocJar by tasks.registering(Jar::class) {
            archiveClassifier.set("javadoc")
            from(tasks.named("javadoc"))
        }

        // Configure publishing
        extensions.configure<org.gradle.api.publish.PublishingExtension>("publishing") {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])

                    artifact(sourcesJar.get())
                    artifact(javadocJar.get())

                    groupId = project.group as String
                    artifactId = project.name
                    version = project.version as String

                    pom {
                        name.set(project.name)
                        description.set(project.description)
                    }
                }
            }

            repositories {
                mavenLocal()
            }
        }
    }
}
