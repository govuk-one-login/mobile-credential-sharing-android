import com.android.build.api.dsl.LibraryExtension

plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

configure<LibraryExtension> {
    namespace = "$namespacePrefix.prerequisites.api"
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
        libs.com.google.test.parameter.injector
    ).forEach(::testFixturesApi)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set("GOV.UK One Login Wallet Sharing: Prerequisite gate API")
        description.set(
            """
            Provides the prerequisite gate contract. Performs checks before starting a journey.
            """.trimIndent()
        )
    }
}
