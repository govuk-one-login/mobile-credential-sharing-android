plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.analytics"
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

    buildFeatures {
        compose = false
    }
}

dependencies {
    listOf(
        libs.uk.gov.logging.api,
        projects.core
    ).forEach(::api)

    listOf(
        libs.com.google.test.parameter.injector,
        testFixtures(libs.uk.gov.logging.api)
    ).forEach(::testFixturesApi)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Analytics"
        )
        description.set(
            """
            A module for holding analytics configuration that's common throughout the wallet
            sharing code base.
            """.trimIndent()
        )
    }
}

tasks.matching {
    it.name.contains("ComposePreview", ignoreCase = true) ||
        it.name.contains("Screenshot", ignoreCase = true)
}.configureEach {
    enabled = false
}
