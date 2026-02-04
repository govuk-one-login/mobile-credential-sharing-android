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
        projects.core
    ).forEach(::implementation)

    listOf(
        libs.kotlinx.coroutines
    ).forEach(::api)

    listOf(
        libs.junit,
        libs.kotlinx.coroutines.test
    ).forEach(::testImplementation)

    listOf(
        projects.core
    ).forEach(::testFixturesImplementation)
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
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
