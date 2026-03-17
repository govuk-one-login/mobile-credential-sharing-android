package uk.gov.onelogin.sharing.orchestration

import android.Manifest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker.Companion.bluetoothPermissions
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

/**
 * Implements [Resettable] for clearing internal state, such as the session state machines.
 */
interface Orchestrator : Resettable {
    /**
     * Begins the User journey.
     */
    fun start()

    /**
     * Completes the User journey.
     *
     * Specifically, this represents the User choosing to prematurely end the journey, as opposed to
     * fully completing, or ending due to unrecoverable errors.
     */
    fun cancel()

    interface Holder : Orchestrator {
        val holderSessionState: StateFlow<HolderSessionState>

        companion object {
            const val JOURNEY_NAME: String = "holder"
        }
    }
    interface Verifier : Orchestrator {
        val verifierSessionState: Flow<VerifierSessionState>

        fun processQrCode(qrCode: BarcodeDataResult)

        companion object {
            const val JOURNEY_NAME: String = "verifier"
            val requiredPermissions = bluetoothPermissions() + Manifest.permission.CAMERA
        }
    }

    /**
     * Property bag object containing logging messages common to [Orchestrator] implementations.
     */
    data object LogMessages {
        const val START_ORCHESTRATION_ERROR: String = "Cannot start orchestration"
        const val START_ORCHESTRATION_SUCCESS: String = "start orchestration"
        const val CANNOT_TRANSITION_TO_STATE: String = "Cannot transition to state:"
        const val TRANSITION_SUCCESSFUL_TO_STATE: String = "Transition successful to state:"

        fun completedPrerequisiteChecks(journey: String, response: Any?): String =
            "Performed $journey prerequisite checks: $response"

        fun createSessionResetMessage(journey: String): String =
            "Cleared Orchestrator $journey session"

        fun recreateSessionOnStartMessage(journey: String): String =
            "Starting an Orchestrator $journey session after completing the previous journey"
    }
}
