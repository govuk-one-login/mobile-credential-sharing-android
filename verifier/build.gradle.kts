plugins {
    listOf(
        libs.plugins.android.library,
        libs.plugins.kotlin.android,
        libs.plugins.kotlin.compose,
    ).forEach(::alias)
}

val androidCompileSdk: Int by rootProject.extra
val androidMinSdk: Int by rootProject.extra
val androidTargetSdk: Int by rootProject.extra
val namespacePrefix: String by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra


android {
    namespace = "$namespacePrefix.verifier"
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
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    kotlinOptions {
        jvmTarget = javaVersion.majorVersion
    }
    testOptions {
        targetSdk = androidTargetSdk
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    listOf(
        platform(libs.androidx.compose.bom),
        libs.bundles.testing.instrumentation,
    ).forEach(::androidTestImplementation)

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