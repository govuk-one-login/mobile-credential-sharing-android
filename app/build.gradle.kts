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
        libs.plugins.hilt.plugin,
        libs.plugins.metro.di
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

        testInstrumentationRunner = "uk.gov.onelogin.sharing.testapp.di.CustomTestRunner"
    }
    buildFeatures {
        compose = true
    }
    signingConfigs {
        create("release") {
            val configDir = rootProject.extra["configDir"]

            storeFile = file("$configDir/keystore.jks")

            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        libs.bundles.testing.instrumentation,
        libs.hilt.android.testing,
        libs.uk.gov.logging.testdouble,
        testFixtures(projects.orchestration)
    ).forEach(::androidTestImplementation)

    listOf(
        libs.hilt.compiler
    ).forEach(::kspAndroidTest)

    listOf(
        projects.core, // Remove once SDK prerequisites screen handles permissions
        projects.sdk,
        projects.ui.uiApi,
        projects.ui.uiImpl
    ).forEach(::implementation)

    listOf(
        libs.bundles.debug.tooling
    ).forEach(::debugImplementation)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.android.baseline,
        libs.bundles.uk.gov.ui,
        libs.hilt.android,
        libs.uk.gov.logging.impl,
        libs.uk.gov.logging.api,
        testFixtures(projects.verifier)
    ).forEach(::implementation)
    ksp(libs.hilt.compiler)

    listOf(
        libs.androidx.test.rules,
        libs.androidx.ui.test.junit4,
        libs.com.google.test.parameter.injector,
        testFixtures(projects.holder),
        testFixtures(projects.ui.uiImpl),
        testFixtures(projects.verifier),
        testFixtures(projects.sdk)
    ).forEach(::testFixturesApi)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.androidx.navigation.testing,
        libs.bundles.android.baseline,
        testFixtures(projects.sdk),
        testFixtures(projects.ui.uiApi),
        testFixtures(projects.ui.uiImpl)
    ).forEach(::testFixturesImplementation)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.testing.unit,
        libs.metro.runtime,
        libs.uk.gov.logging.testdouble,
        testFixtures(projects.core), // Remove once SDK prerequisites screen handles permissions
        testFixtures(projects.holder),
        testFixtures(projects.orchestration),
        testFixtures(projects.sdk)
    ).forEach(::testImplementation)
}
