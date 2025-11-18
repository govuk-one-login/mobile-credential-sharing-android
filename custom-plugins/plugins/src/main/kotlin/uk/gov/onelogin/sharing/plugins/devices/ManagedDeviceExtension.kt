package uk.gov.onelogin.sharing.plugins.devices

import com.android.build.api.variant.impl.capitalizeFirstChar
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.internal.extensions.stdlib.capitalized

/**
 *
 */
interface ManagedDeviceExtension {
    /**
     * Use device profiles you typically see in Android Studio.
     *
     * @see com.android.build.api.dsl.ManagedVirtualDevice.device
     */
    val deviceProfile: Property<String>

    // Use only API levels 27 and higher.
    /**
     * @see com.android.build.api.dsl.ManagedVirtualDevice.apiLevel
     */
    val apiLevel: Property<Int>

    /**
     * To include Google services, use "google-atd" instead
     *
     * @see com.android.build.api.dsl.ManagedVirtualDevice.systemImageSource
     */
    val systemImageSource: Property<String>
    /**
     * @see com.android.build.api.dsl.ManagedVirtualDevice.require64Bit
     */
    val requires64Bit: Property<Boolean>

    /**
     * @see com.android.build.api.dsl.ManagedVirtualDevice.testedAbi
     */
    val testedAbi: Property<String>

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
            testedAbi.convention("x86_64")
        }
    }
}