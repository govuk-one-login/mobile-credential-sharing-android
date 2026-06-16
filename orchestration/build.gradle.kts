plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.orchestration"
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
}

dependencies {
    listOf(
        libs.androidx.camera.lifecycle,
        projects.bluetooth,
        projects.core,
        projects.cameraService,
        projects.cryptoService,
        projects.credentialVerification,
        projects.prerequisiteGateApi
    ).forEach(::api)

    listOf(
        libs.com.google.guava.android,
        libs.bundles.androidx.camera,
        libs.kotlinx.serialization.json,
        libs.metro.viewmodel.compose,
        projects.exchangeFormat,
        projects.prerequisiteGateImpl
    ).forEach(::implementation)

    listOf(
        libs.com.google.test.parameter.injector,
        libs.junit,
        projects.cameraService,
        projects.exchangeFormat,
        testFixtures(projects.cryptoService),
        testFixtures(projects.credentialFormat),
        testFixtures(projects.exchangeFormat),
        testFixtures(projects.prerequisiteGateApi)
    ).forEach(::testFixturesApi)

    listOf(
        libs.mockk
    ).forEach(::testFixturesImplementation)

    listOf(
        libs.bundles.androidx.camera,
        testFixtures(projects.bluetooth),
        testFixtures(projects.core),
        testFixtures(projects.cryptoService)
    ).forEach(::testImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set("GOV.UK One Login Wallet Sharing: Digital Credential Orchestrator")
        description.set(
            """
            Provides the Orchestration layer.
            """.trimIndent()
        )
    }
}
