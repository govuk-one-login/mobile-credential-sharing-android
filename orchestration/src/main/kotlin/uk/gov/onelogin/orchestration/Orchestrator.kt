package uk.gov.onelogin.orchestration

import android.Manifest
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothCentralPermissionChecker.Companion.centralPermissions
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.AuthorizationResponse

/**
 * Implements [Resettable] for clearing internal state, such as the session state machines.
 */
interface Orchestrator : Resettable {

    /**
     * Begins the User journey.
     *
     * @param requiredPermissions The Android permissions required to successfully complete the
     * User journey.
     */
    fun start(requiredPermissions: Set<String>)

    /**
     * Completes the User journey.
     *
     * Specifically, this represents the User choosing to prematurely end the journey, as opposed to
     * fully completing, or ending due to unrecoverable errors.
     */
    fun cancel()

    interface Holder : Orchestrator {
        companion object {
            const val JOURNEY_NAME: String = "holder"
        }
    }
    interface Verifier : Orchestrator {
        companion object {
            const val JOURNEY_NAME: String = "verifier"
            val requiredPermissions = centralPermissions() + Manifest.permission.CAMERA
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

        fun completedAuthorizationCheck(journey: String, response: AuthorizationResponse): String =
            "Performed $journey authorization check: $response"

        fun createSessionResetMessage(journey: String): String =
            "Cleared Orchestrator $journey session"

        fun recreateSessionOnStartMessage(journey: String): String =
            "Starting an Orchestrator $journey session after completing the previous journey"
    }
}
