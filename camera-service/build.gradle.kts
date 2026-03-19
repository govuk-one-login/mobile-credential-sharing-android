plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.cameraService"
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
        projects.core
    ).forEach(::implementation)

    listOf(
        projects.core
    ).forEach(::testFixturesApi)

    listOf(
        libs.com.google.test.parameter.injector,
        testFixtures(projects.core),
        testFixtures(projects.cryptoService),
        testFixtures(libs.uk.gov.ui.android.componentsv2.camera)
    ).forEach(::testFixturesImplementation)

    listOf(
        testFixtures(projects.core),
        testFixtures(libs.uk.gov.ui.android.componentsv2.camera)
    ).forEach(::testImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Camera Service"
        )
        description.set(
            """
            A module for handling interpretation of QR codes.
            """.trimIndent()
        )
    }
}
