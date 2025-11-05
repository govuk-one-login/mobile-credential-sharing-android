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
