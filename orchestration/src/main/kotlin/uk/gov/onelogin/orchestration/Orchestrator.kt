package uk.gov.onelogin.orchestration

import android.Manifest
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker.Companion.bluetoothPermissions
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

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
        val holderSessionState: SharedFlow<HolderSessionState>

        companion object {
            const val JOURNEY_NAME: String = "holder"
        }
    }
    interface Verifier : Orchestrator {
        companion object {
            const val JOURNEY_NAME: String = "verifier"
            val requiredPermissions = bluetoothPermissions() + Manifest.permission.CAMERA
        }
    }

    /**
     * Property bag object containing logging messages common to [Orchestrator] implementations.
     */
    data object LogMessages {
        const val CANCEL_ORCHESTRATION_ERROR: String = "Cannot cancel orchestration"
        const val CANCEL_ORCHESTRATION_SUCCESS: String = "cancel orchestration"
        const val START_ORCHESTRATION_ERROR: String = "Cannot start orchestration"
        const val START_ORCHESTRATION_SUCCESS: String = "start orchestration"

        fun completedPrerequisiteChecks(journey: String, response: Any?): String =
            "Performed $journey prerequisite checks: $response"

        fun createSessionResetMessage(journey: String): String =
            "Cleared Orchestrator $journey session"

        fun recreateSessionOnStartMessage(journey: String): String =
            "Starting an Orchestrator $journey session after completing the previous journey"
    }
}
