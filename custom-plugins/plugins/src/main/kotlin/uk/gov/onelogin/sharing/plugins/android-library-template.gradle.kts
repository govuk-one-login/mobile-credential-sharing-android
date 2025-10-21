package uk.gov.onelogin.sharing.plugins

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

listOf(
    "android-library",
    "kotlin-android",
    "kotlin-compose",
    "custom-language.config",
    "custom-managed.devices",
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
val testFixturesImplementation by configurations
val testImplementation by configurations


dependencies {
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
    }

    listOf(
        "android-baseline",
    ).map { libs.findBundle(it).get() }.forEach {
        implementation(it)
        testFixturesImplementation(it)
    }

    listOf(
        "testing-unit",
    ).map { libs.findBundle(it).get() }.forEach {
        testImplementation(it)
    }
}