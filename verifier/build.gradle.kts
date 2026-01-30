plugins {
    listOf(
        libs.plugins.metro.di,
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.verifier"
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
        projects.bluetooth,
        projects.core,
        projects.models,
        projects.security,
        projects.orchestration,
        projects.sdk
    ).forEach(::api)

    listOf(
        libs.metro.viewmodel.compose
    ).forEach(::implementation)

    implementation(libs.androidx.browser)
    listOf(
        libs.androidx.browser,
        libs.com.google.test.parameter.injector,
        testFixtures(libs.uk.gov.ui.android.componentsv2.camera),
        testFixtures(projects.bluetooth),
        testFixtures(projects.core),
        testFixtures(projects.security)
    ).forEach(::testFixturesImplementation)

    listOf(
        testFixtures(libs.uk.gov.ui.android.componentsv2.camera),
        testFixtures(projects.core),
        testFixtures(projects.bluetooth),
        testFixtures(projects.sdk)
    ).forEach(::testImplementation)

    listOf(
        testFixtures(projects.sdk)
    ).forEach(::androidTestImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Credential Verifier"
        )
        description.set(
            """
            Provides functionality for apps to validate digital identification
            credentials. This acts as the assurance mechanism for compliant digital credentials.
            """.trimIndent()
        )
    }
}
