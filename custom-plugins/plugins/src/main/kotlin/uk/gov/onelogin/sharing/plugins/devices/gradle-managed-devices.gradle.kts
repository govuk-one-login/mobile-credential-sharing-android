package uk.gov.onelogin.sharing.plugins.devices

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.TestOptions
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidApp
import uk.gov.onelogin.sharing.plugins.PluginManagerExtensions.isAndroidLibrary
import uk.gov.onelogin.sharing.plugins.devices.ManagedDeviceExtension.Companion.createManagedDeviceConfig

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
val configuration = createManagedDeviceConfig()

val androidTestImplementation by project.configurations
val androidTestUtil by project.configurations

dependencies {
    androidTestUtil(libs.findLibrary("androidx-test-orchestrator").get().get())
    androidTestImplementation(libs.findLibrary("androidx-test-runner").get().get())
}

if (pluginManager.isAndroidApp()) {
    configure<ApplicationExtension> {
        testOptions {
            configureManagedDeviceTestOptions()
        }
    }
} else if (pluginManager.isAndroidLibrary()) {
    configure<LibraryExtension> {
        testOptions {
            configureManagedDeviceTestOptions()
        }
    }
}

/**
 * Configure the android plugin based on enabling
 * [Gradle managed devices](https://developer.android.com/studio/test/gradle-managed-devices).
 */
private fun TestOptions.configureManagedDeviceTestOptions() {
    animationsDisabled = true
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    managedDevices {
        localDevices {
            create(configuration.name()) {
                // Use device profiles you typically see in Android Studio.
                this.device = configuration.deviceProfile.get()
                // Use only API levels 27 and higher.
                this.apiLevel = configuration.apiLevel.get()
                // To include Google services, use "google-atd" instead
                this.systemImageSource = configuration.systemImageSource.get()
                this.require64Bit = configuration.requires64Bit.get()
                this.testedAbi = configuration.testedAbi.get()
            }
        }
    }
}
