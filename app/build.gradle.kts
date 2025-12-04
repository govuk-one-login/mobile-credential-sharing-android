import uk.gov.onelogin.sharing.plugins.Filters.licenseFilters

plugins {
    listOf(
        libs.plugins.android.application,
        libs.plugins.kotlin.android,
        libs.plugins.kotlin.compose,
        libs.plugins.kotlin.parcelize,
        libs.plugins.kotlin.serialization,
        libs.plugins.custom.language.config,
        libs.plugins.custom.managed.devices,
        libs.plugins.roborazzi,
        libs.plugins.screenshot.testing,
        libs.plugins.android.lint.config,
        libs.plugins.spotless.config,
        libs.plugins.detekt.config,
        libs.plugins.test.coverage,
        libs.plugins.sonar.module.config,
        libs.plugins.kotlin.ksp,
        libs.plugins.hilt.plugin
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
    packaging {
        licenseFilters.forEach(resources.excludes::plusAssign)
    }
}

dependencies {
    listOf(
        platform(libs.androidx.compose.bom),
        libs.androidx.navigation.testing,
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
        libs.bundles.android.baseline,
        libs.bundles.uk.gov.ui,
        libs.hilt.android,
        testFixtures(projects.verifier)
    ).forEach(::implementation)
    ksp(libs.hilt.compiler)

    listOf(
        libs.androidx.test.rules,
        libs.androidx.ui.test.junit4,
        libs.com.google.test.parameter.injector,
        testFixtures(projects.holder),
        testFixtures(projects.verifier)
    ).forEach(::testFixturesApi)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.androidx.navigation.testing,
        libs.bundles.android.baseline
    ).forEach(::testFixturesImplementation)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.testing.unit,
        testFixtures(projects.holder)
    ).forEach(::testImplementation)
}
