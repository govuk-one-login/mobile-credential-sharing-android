plugins {
    listOf(
        libs.plugins.templates.android.library,
        libs.plugins.metro.di
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.bluetooth"
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
        libs.metro.viewmodel.compose
    ).forEach(::implementation)

    testImplementation(testFixtures(projects.core))
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Bluetooth"
        )
        description.set(
            """
            A module for handling the connections between devices when sharing
            digital credentials.
            """.trimIndent()
        )
    }
}
