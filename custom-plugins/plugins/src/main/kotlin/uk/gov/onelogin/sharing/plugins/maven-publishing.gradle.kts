package uk.gov.onelogin.sharing.plugins

import com.android.build.api.dsl.LibraryExtension
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidLibrary
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isJavaLibrary
import java.net.URI

plugins {
    `maven-publish`
}

val githubRepositoryName = "mobile-credential-sharing-android"

if (pluginManager.isAndroidLibrary()) {
    configure<LibraryExtension> {
        publishing {
            multipleVariants {
                allVariants()
                withJavadocJar()
                withSourcesJar()
            }
        }
    }

    configure<PublishingExtension> {
        publications {
            register<MavenPublication>("default") {
                this.artifactId = project.name
                this.version = project.version.toString()

                repositories {
                    configureMavenRepositoriesToPublishTo()
                }

                project.afterEvaluate {
                    // multipleVariants publish under the "default" name
                    from(project.components["default"])
                    withBuildIdentifier()
                }

                pom {
                    defaultPomSetup()
                }
            }
        }
    }


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
} else if (pluginManager.isJavaLibrary()) {

}

/**
 * Configures the locations to publish to:
 *
 * - The One Login GitHub packages registry
 * - The `project.buildDir/repo` directory for locally verifying the contents that'll upload
 *   to the GitHub packages registry.
 */
private fun RepositoryHandler.configureMavenRepositoriesToPublishTo() {
    maven {
        name = "GitHubPackages"
        url =
            URI.create(
                "https://maven.pkg.github.com/govuk-one-login/$githubRepositoryName",
            )
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        name = "localBuild"
        url = layout.buildDirectory.dir("repo").map {
            it.asFile.toURI()
        }.get()
    }
}

/**
 * Configures the generated Maven Project Object Model (POM) based on the [extension]
 * configuration.
 */
private fun MavenPom.defaultPomSetup() {
    this.name.set(project.name)
    this.description.set(project.description)

    this.licenses {
        this.license {
            this.name.set("MIT License")
            this.url.set("https://choosealicense.com/licenses/mit/")
        }
    }
    this.developers {
        this.developer {
            this.id.set("walletSharingTeam")
            this.name.set("Wallet Sharing Team")
            this.email.set("wallet-sharing@digital.cabinet-office.gov.uk")
        }
    }

    this.scm {
        val baseUrl = "github.com/govuk-one-login/$githubRepositoryName"
        this.connection.set(
            "scm:git:git://$baseUrl.git"
        )
        this.developerConnection.set(
            "scm:git:ssh://$baseUrl.git"
        )
        this.url.set(
            "https://$baseUrl"
        )
    }
}
