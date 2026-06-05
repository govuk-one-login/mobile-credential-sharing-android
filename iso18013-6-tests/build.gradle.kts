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

    listOf(
        libs.jackson.cbor,
        libs.jackson.core,
        libs.jackson.kotlin,
        libs.junit,
        libs.kotlin.test,
        projects.bluetooth,
        projects.core,
        projects.cryptoService,
        projects.holder,
        projects.verifier
    ).forEach(::testImplementation)
}
