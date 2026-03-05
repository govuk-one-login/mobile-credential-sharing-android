package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse

@ContributesBinding(AppScope::class)
class CapabilityPrerequisiteLayer(private val context: Context, private val logger: Logger) :
    PrerequisiteGateLayer.Capability {
    override fun checkCapability(prerequisite: Prerequisite): PrerequisiteResponse.Incapable? =
        when (prerequisite) {
            Prerequisite.BLUETOOTH -> handleBluetoothCapability()
            Prerequisite.CAMERA -> handleCameraCapability()
            Prerequisite.UNKNOWN -> null
        }.also {
            logger.debug(
                logTag,
                "Performed $prerequisite capability check. Response: $it"
            )
        }

    private fun handleBluetoothCapability(): PrerequisiteResponse.Incapable? = if (
        (
            context.getSystemService(
                Context.BLUETOOTH_SERVICE
            ) as? BluetoothManager
            )?.adapter == null
    ) {
        PrerequisiteResponse.Incapable(IncapableReason.MissingHardware)
    } else {
        null
    }

    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    private fun handleCameraCapability(): PrerequisiteResponse.Incapable? = if (
        context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)
    ) {
        null
    } else {
        PrerequisiteResponse.Incapable(IncapableReason.MissingHardware)
    }
}
