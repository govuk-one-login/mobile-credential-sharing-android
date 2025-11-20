package uk.gov.onelogin.sharing.plugins.publishing

import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register

object PublishingCustomTasks {
    fun Project.createLocalBuildMavenRepositoryTask() {
        tasks.register<Copy>("createLocalBuildMavenRepository") {
            group = "publishing"
            description = "Copies locally created artefacts into the root project's build directory."
            dependsOn(
                "publishAllPublicationsToLocalBuildRepository",
            )

            from(layout.buildDirectory.dir("repo"))
            val destDir = rootProject.layout.buildDirectory.dir("repo")
            into(destDir)

            doLast {
                logger.lifecycle(
                    "Copied publishable artefacts into ${destDir.get()}"
                )
            }
        }
    }

    // https://govukverify.atlassian.net/browse/DCMAW-11888
    // https://github.com/Kotlin/dokka/issues/2956
    fun Project.disableJavadocGeneration() {
        tasks
            .matching { task ->
                task.name.contains("javaDocReleaseGeneration") ||
                        task.name.contains("javaDocDebugGeneration")
            }.configureEach {
                enabled = false
            }
    }
}