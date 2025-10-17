package uk.gov.onelogin.sharing.plugins.devices

import com.android.build.api.variant.impl.capitalizeFirstChar
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.internal.extensions.stdlib.capitalized

/**
 *
 */
interface ManagedDeviceExtension {
    // Use device profiles you typically see in Android Studio.
    val deviceProfile: Property<String>

    // Use only API levels 27 and higher.
    val apiLevel: Property<Int>

    // To include Google services, use "google-atd" instead
    val systemImageSource: Property<String>
    val requires64Bit: Property<Boolean>

    fun name(): String {
        val systemImageArray = systemImageSource.get().split("-")
        val result = StringBuilder(systemImageArray[0])
        result.append(
            systemImageArray.takeLast(systemImageArray.size - 1).joinToString {
                it.capitalizeFirstChar()
            }
        ).append("Api")
            .append(apiLevel.get())

        result.append(
            deviceProfile.get().replace(" ", "").capitalizeFirstChar()
        )

        return result.toString()
    }

    companion object {
        fun Project.createManagedDeviceConfig() = extensions.create(
            "managedDeviceConfig",
            ManagedDeviceExtension::class.java,
        ).apply {
            deviceProfile.convention("Pixel 9")
            // Use only API levels 27 and higher.
            apiLevel.convention(34)
            // To include Google services, use "google-atd" instead
            // Otherwise, use "aosp-atd"
            systemImageSource.convention("aosp-atd")
            requires64Bit.convention(false)
        }
    }
}