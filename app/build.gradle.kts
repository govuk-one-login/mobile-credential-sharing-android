plugins {
    listOf(
        libs.plugins.android.application,
        libs.plugins.kotlin.android,
        libs.plugins.kotlin.compose,
        libs.plugins.custom.language.config,
        libs.plugins.custom.managed.devices,
        libs.plugins.roborazzi,
        libs.plugins.screenshot.testing,
        libs.plugins.android.lint.config,
        libs.plugins.spotless.config,
        libs.plugins.detekt.config,
        libs.plugins.test.coverage,
        libs.plugins.sonar.module.config
    ).forEach { alias(it) }
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val androidTargetSdk: Int by rootProject.extra
val androidVersionCode: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra

private val appId = "$namespacePrefix.testapp"

android {
    namespace = appId
    compileSdk = androidCompileSdk

    defaultConfig {
        applicationId = appId
        minSdk = androidMinSdk
        targetSdk = androidTargetSdk
        versionCode = androidVersionCode
        versionName = project.version.toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
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
        platform(libs.androidx.compose.bom),
        libs.bundles.testing.instrumentation
    ).forEach(::androidTestImplementation)

    listOf(
        projects.holder,
        projects.verifier
    ).forEach(::api)

    listOf(
        libs.bundles.debug.tooling
    ).forEach(::debugImplementation)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.android.baseline
    ).forEach(::implementation)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.android.baseline
    ).forEach(::testFixturesImplementation)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.testing.unit,
        testFixtures(projects.holder)
    ).forEach(::testImplementation)
}
