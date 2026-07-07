import com.android.build.api.dsl.LibraryExtension

plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

configure<LibraryExtension> {
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
        libs.org.bouncycastle.bcprov.jdk18on,
        libs.org.bouncycastle.bctls.jdk18on,
        projects.bluetooth,
        projects.core,
        projects.cryptoService,
        projects.holder,
        projects.verifier,
        testFixtures(projects.cryptoService)
    ).forEach(::testImplementation)
}
