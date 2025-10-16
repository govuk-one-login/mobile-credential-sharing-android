plugins {
}

    android {
    namespace = "uk.gov.onelogin.sharing.bluetooth"
    compileSdk = 36

    defaultConfig {
    minSdk = 29

      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
       release {
           isMinifyEnabled = false
           proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
       }
    }
    }

  dependencies {

  }