plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.iso18013_6_tests"
    compileSdk = androidCompileSdk

    defaultConfig {
        minSdk = androidMinSdk
    }
}

dependencies {
    testImplementation(projects.models)
    testImplementation(projects.core)
    testImplementation(projects.bluetooth)
    testImplementation(projects.holder)
    testImplementation(projects.verifier)
    testImplementation(projects.cryptoService)

    listOf(
        libs.junit,
        libs.kotlin.test
    ).forEach(::testImplementation)
}
