package uk.gov.onelogin.sharing.orchestration.prerequisites.capability

import android.content.Context
import androidx.camera.core.CameraSelector
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.ContextExt.bluetoothManager
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.camera.ProcessCameraProviderFactory

@ContributesBinding(AppScope::class)
class CapabilityPrerequisiteLayer(
    private val context: Context,
    private val factory: ProcessCameraProviderFactory,
    private val logger: Logger
) : PrerequisiteGateLayer.Capability {
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
        context.bluetoothManager?.adapter == null
    ) {
        PrerequisiteResponse.Incapable(IncapableReason.MissingHardware)
    } else {
        null
    }

    private fun handleCameraCapability(): PrerequisiteResponse.Incapable? = runCatching {
        factory.create()
    }.mapCatching { provider ->
        provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)
    }.fold(
        onSuccess = { condition ->
            if (condition) {
                null
            } else {
                PrerequisiteResponse.Incapable(IncapableReason.MissingHardware)
            }
        },
        onFailure = { PrerequisiteResponse.Incapable(IncapableReason.CannotCheckCamera) }
    )
}
