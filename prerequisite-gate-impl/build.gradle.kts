plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.prerequisites"
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
        projects.prerequisiteGateApi,
        projects.core,
    ).forEach(::api)

    listOf(
        libs.androidx.camera.lifecycle,
        projects.bluetooth,
        libs.metro.viewmodel.compose,
        ).forEach(::implementation)

    listOf(
        testFixtures(projects.core)
    ).forEach(::testImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set("GOV.UK One Login Wallet Sharing: Prerequisite gate implementation")
        description.set(
            """
            Provides implementations for the prerequisite gate contract.
            """.trimIndent()
        )
    }
}
