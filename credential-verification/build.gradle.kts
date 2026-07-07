plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}
val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.credentialVerification"
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
        projects.credentialFormat
    ).forEach(::api)

    listOf(
        libs.jackson.cbor,
        projects.exchangeFormat
    ).forEach(::implementation)

    listOf(
        libs.org.hamcrest,
        testFixtures(projects.credentialFormat)
    ).forEach(::testFixturesApi)

    listOf(
        libs.com.google.test.parameter.injector,
        libs.jackson.cbor,
        libs.junit,
        libs.org.bouncycastle.bcpkix.jdk18on
    ).forEach(::testFixturesImplementation)

    listOf(
        libs.com.google.test.parameter.injector,
        libs.io.github.classgraph,
        libs.junit,
        libs.mockk,
        libs.org.hamcrest,
        projects.exchangeFormat
    ).forEach(::testImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Verification module"
        )
        description.set(
            """
            Provides data structures and business logic for handling document verifications.
            """.trimIndent()
        )
    }
}
