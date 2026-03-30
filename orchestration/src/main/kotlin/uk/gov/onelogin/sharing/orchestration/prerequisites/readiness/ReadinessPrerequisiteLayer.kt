package uk.gov.onelogin.sharing.orchestration.prerequisites.readiness

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.ContextExt.bluetoothManager
import uk.gov.onelogin.sharing.core.logger.logTag
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteReason
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteGateLayer
import uk.gov.onelogin.sharing.orchestration.prerequisites.camera.ProcessCameraProviderFactory

@ContributesBinding(AppScope::class)
class ReadinessPrerequisiteLayer(
    private val context: Context,
    private val factory: ProcessCameraProviderFactory,
    private val logger: Logger
) : PrerequisiteGateLayer.Readiness {
    override fun checkReadiness(prerequisite: Prerequisite): MissingPrerequisiteReason.NotReady? =
        when (prerequisite) {
            Prerequisite.BLUETOOTH -> handleBluetoothReadiness()
            Prerequisite.CAMERA -> handleCameraReadiness()
            Prerequisite.UNKNOWN -> null
        }.also {
            logger.debug(
                logTag,
                "Performed $prerequisite readiness check. Response: $it"
            )
        }

    private fun handleBluetoothReadiness(): MissingPrerequisiteReason.NotReady? = if (
        context.bluetoothManager?.adapter?.isEnabled ?: false
    ) {
        null
    } else {
        MissingPrerequisiteReason.NotReady(NotReadyReason.BluetoothTurnedOff)
    }

    private fun handleCameraReadiness(): MissingPrerequisiteReason.NotReady? = runCatching {
        factory.create()
    }.mapCatching { provider ->
        provider.getCameraInfo(CameraSelector.DEFAULT_BACK_CAMERA).cameraState.value?.type
    }.fold(
        onSuccess = { state ->
            if (state == null || CameraState.Type.CLOSED == state) {
                null
            } else {
                MissingPrerequisiteReason.NotReady(NotReadyReason.CameraAlreadyInUse)
            }
        },
        onFailure = { MissingPrerequisiteReason.NotReady(NotReadyReason.CannotCheckCamera) }
    )
}
