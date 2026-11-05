plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.screenshottesting"
    compileSdk = androidCompileSdk

    defaultConfig {
        minSdk = androidMinSdk
    }
}

dependencies {
    // Shared screenshot-test infra, exposed via testFixtures. Test-only; never published.
    listOf(
        libs.bundles.screenshot.testing,
        libs.androidx.ui.test.junit4,
        libs.com.google.test.parameter.injector
    ).forEach(::testFixturesImplementation)
}
