package uk.gov.onelogin.sharing.plugins

import com.android.build.api.dsl.LibraryExtension
import uk.gov.onelogin.sharing.plugins.Filters.licenseFilters
import uk.gov.onelogin.sharing.plugins.publishing.PublishingCustomTasks.createLocalBuildMavenRepositoryTask
import uk.gov.onelogin.sharing.plugins.publishing.PublishingCustomTasks.disableJavadocGeneration

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

listOf(
    "android-library",
    "kotlin-compose",
    "kotlin-parcelize",
    "kotlin-serialization",
    "custom-language.config",
    "custom-managed.devices",
    "metro-di",
    "roborazzi",
    "screenshot-testing",
    "android-lint.config",
    "spotless-config",
    "detekt-config",
    "test-coverage",
    "sonar-module-config",
).map { versionCatalogId ->
    libs.findPlugin(versionCatalogId).get().get().pluginId
}.forEach(pluginManager::apply)

listOf(
    "uk.gov.publishing.config"
).forEach(pluginManager::apply)

val androidTestImplementation by configurations
val debugImplementation by configurations
val implementation by configurations
val testFixturesApi by configurations
val testFixturesImplementation by configurations
val testReleaseImplementation by configurations
val testImplementation by configurations


dependencies {
    modules {
        listOf(
            "hamcrest-library",
            "hamcrest-core"
        ).forEach { dependency ->
            module("org.hamcrest:$dependency") {
                replacedBy(
                    "org.hamcrest:hamcrest",
                    "Deprecated by the JavaHamcrest team"
                )
            }
        }
    }

    platform(libs.findLibrary("androidx.compose.bom").get()).let {
        androidTestImplementation(it)
        implementation(it)
        testFixturesImplementation(it)
        testImplementation(it)
    }

    listOf(
        "testing-instrumentation",
    ).map { libs.findBundle(it).get() }.forEach {
        androidTestImplementation(it)
    }

    listOf(
        "debug-tooling",
    ).map { libs.findBundle(it).get() }.forEach {
        debugImplementation(it)
        testReleaseImplementation(it)
    }

    listOf(
        "androidx-test-rules",
        "androidx-ui-test-junit4",
        "androidx-test-espresso-intents"
    ).map { libs.findLibrary(it).get().get() }.forEach {
        testFixturesImplementation(it)
    }

    listOf(
        "android-baseline",
        "uk-gov-logging",
        "uk-gov-ui",
    ).map { libs.findBundle(it).get() }.forEach {
        implementation(it)
        testFixturesImplementation(it)
    }

    listOf(
        "uk-gov-ui-android-componentsv2",
        "uk-gov-ui-android-patterns",
        "uk-gov-ui-android-theme",
    ).map { libs.findLibrary(it).get() }
        .map(::testFixtures)
        .forEach {
            testFixturesImplementation(it)
        }

    listOf(
        "testing-unit",
    ).map { libs.findBundle(it).get() }.forEach {
        testImplementation(it)
    }
}

createLocalBuildMavenRepositoryTask()

project.disableJavadocGeneration()

configure<LibraryExtension> {
    packaging {
        licenseFilters.forEach(resources.excludes::plusAssign)
    }
}
