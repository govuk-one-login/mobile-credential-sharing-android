plugins {
    listOf(
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
        projects.core,
        projects.models,
        projects.security
    ).forEach(::api)

    listOf(
        testFixtures(projects.security)
    ).forEach(::testFixturesApi)

    implementation(libs.androidx.browser)
    listOf(
        libs.androidx.browser,
        testFixtures(projects.core)
    ).forEach(::testFixturesImplementation)

    listOf(
        libs.com.google.test.parameter.injector,
        testFixtures(libs.uk.gov.ui.android.componentsv2.camera),
        testFixtures(projects.core)
    ).forEach(::testImplementation)
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
