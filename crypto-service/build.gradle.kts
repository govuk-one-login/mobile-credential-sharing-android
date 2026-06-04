plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}
val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.cryptoService"
    compileSdk = androidCompileSdk

    defaultConfig {
        minSdk = androidMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

dependencies {
    listOf(
        libs.jackson.cbor
    ).forEach(::api)

    listOf(
        libs.jackson.core,
        libs.jackson.kotlin,
        libs.metro.viewmodel.compose,
        libs.kotlinx.io.bytestring,
        projects.core,
        projects.exchangeFormat
    ).forEach(::implementation)

    listOf(
        projects.exchangeFormat,
        testFixtures(projects.exchangeFormat)
    ).forEach(::testFixturesApi)

    listOf(
        libs.com.google.test.parameter.injector,
        libs.jackson.cbor,
        projects.cryptoService
    ).forEach(::testFixturesImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Digital Credential securities"
        )
        description.set(
            """
            Provides functionality that ensures digital credentials passed between credential
            holders and credential verifiers are done in a secure manner.
            """.trimIndent()
        )
    }
}
