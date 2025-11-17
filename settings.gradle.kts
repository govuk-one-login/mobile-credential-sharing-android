enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        listOf(
            "mobile-android-ui",
        ).forEach { githubRepositoryName ->
            maven {
                url = uri(
                    "https://maven.pkg.github.com/govuk-one-login/$githubRepositoryName"
                )
                setGithubCredentials()
                metadataSources {
                    gradleMetadata()
                }
            }
        }
    }
}

includeBuild("custom-plugins")

rootProject.name = "Mobile-credential-sharing-android"
listOf(
    ":app",
    ":bluetooth",
    ":holder",
    ":models",
    ":security",
    ":verifier",
).forEach(::include)

/**
 * Obtained Github Personal Access Tokens (PATs) from gradle properties.
 *
 * See also:
 * - [Generating a Github PAT](https://govukverify.atlassian.net/wiki/x/J4D9-Q)
 */
fun MavenArtifactRepository.setGithubCredentials() {
    credentials {
        username = System.getenv().getOrElse("GITHUB_ACTOR") {
            providers.gradleProperty("gpr.user").get()
        }
        // Prefer workflow environment variable if it exists
        password = System.getenv().getOrElse("GITHUB_PAT") {
            providers.gradleProperty("gpr.token").get()
        }
    }
}
