plugins {
    listOf(
        libs.plugins.templates.android.library
    ).forEach { alias(it) }
}
val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

android {
    namespace = "$namespacePrefix.sdk"
    compileSdk = androidCompileSdk

    defaultConfig {
        minSdk = androidMinSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    listOf(
        projects.orchestration
    ).forEach(::api)
}

dependencies {
    listOf(
        projects.core,
        projects.orchestration,
        libs.metro.runtime,
        testFixtures(projects.orchestration)
    ).forEach(::testFixturesImplementation)

    listOf(
        testFixtures(projects.orchestration)
    ).forEach(::testImplementation)
}

mavenPublishingConfig {
    mavenConfigBlock {
        name.set(
            "GOV.UK One Login Wallet Sharing: Credential Sharing SDK"
        )
        description.set(
            """
            Provides the SDK for the Wallet Credentials Sharing.
            """.trimIndent()
        )
    }
}
