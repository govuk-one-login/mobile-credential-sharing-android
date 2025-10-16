plugins {
    listOf(
        libs.plugins.android.application,
        libs.plugins.kotlin.android,
        libs.plugins.kotlin.compose,
    ).forEach(::alias)
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val androidTargetSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

private val appId = "$namespacePrefix.testapp"

android {
    namespace = appId
    compileSdk = androidCompileSdk

    defaultConfig {
        applicationId = appId
        minSdk = androidMinSdk
        targetSdk = androidTargetSdk
        versionCode = 1
        versionName = "1.0"

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
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersion.majorVersion
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.testing.instrumentation,
    ).forEach(::androidTestImplementation)

    listOf(
        projects.holder,
    ).forEach(::api)

    listOf(
        libs.bundles.debug.tooling,
    ).forEach(::debugImplementation)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.android.baseline,
    ).forEach(::implementation)

    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.testing.unit,
    ).forEach(::testImplementation)
}
