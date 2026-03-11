plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.uiimpl"
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
        libs.metro.viewmodel.compose,
        projects.holder,
        projects.sdk,
        projects.ui.uiApi,
        projects.verifier
    ).forEach(::implementation)

    listOf(
        projects.sdk,
        projects.ui.uiApi
    ).forEach(::testFixturesApi)

    listOf(
        testFixtures(projects.orchestration),
        testFixtures(projects.sdk)
    ).forEach(::testImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set("GOV.UK One Login Wallet Sharing: UI implementation")
        description.set(
            """
            Provides the UI implementation.
            """.trimIndent()
        )
    }
}
