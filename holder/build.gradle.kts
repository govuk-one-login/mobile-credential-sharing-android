plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.holder"
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
mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Credential Holder"
        )
        description.set(
            """
            Provides functionality for apps to contain digital identification
            credentials. This acts as the data source.
            """.trimIndent()
        )
    }
}

dependencies {
    listOf(
        projects.core,
        projects.models
    ).forEach(::api)

    listOf(
        libs.androidx.lifecycle.viewmodel.compose,
        libs.zxing.core,
        projects.bluetooth,
        projects.core,
        projects.security
    ).forEach(::implementation)

    listOf(
        testFixtures(projects.bluetooth),
        testFixtures(projects.security),
        testFixtures(projects.core)
    ).forEach(::testImplementation)

    listOf(
        projects.bluetooth,
        projects.security,
        testFixtures(projects.bluetooth),
        testFixtures(projects.security),
        testFixtures(projects.core)
    ).forEach(::testFixturesImplementation)
}
