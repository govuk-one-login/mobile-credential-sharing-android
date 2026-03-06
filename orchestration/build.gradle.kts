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
        projects.security
    ).forEach(::api)

    listOf(
        libs.com.google.guava.android,
        libs.bundles.androidx.camera
    ).forEach(::implementation)

    listOf(
        libs.com.google.test.parameter.injector,
        libs.junit
    ).forEach(::testFixturesApi)

    listOf(
        libs.bundles.androidx.camera,
        testFixtures(projects.bluetooth),
        testFixtures(projects.core),
        testFixtures(projects.security)
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
