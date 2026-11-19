package uk.gov.onelogin.sharing.orchestration

import android.Manifest
import kotlinx.coroutines.flow.StateFlow
import uk.gov.onelogin.sharing.core.Resettable
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.prerequisites.api.permissions.BluetoothPermissions.getBluetoothPermissions

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

        /**
         * Begins the User journey, or carries on with the current one. Safe to call whenever the
         * journey's UI is (re)created:
         * - [HolderSessionState.NotStarted]: begins the journey.
         * - [HolderSessionState.Complete] after [dismiss]: begins a fresh session.
         * - [HolderSessionState.Complete] before [dismiss]: does nothing, keeping the result.
         * - Any in progress state: does nothing.
         */
        override fun start()

        /**
         * `true` once the User has dismissed a completed journey via [dismiss], signaling that the
         * host should leave the journey.
         *
         * Cleared when a new session begins via [start] or [reset].
         */
        val isJourneyFinished: StateFlow<Boolean>

        /**
         * Acknowledges the terminal result of a completed journey.
         *
         * Reports [isJourneyFinished] once, without creating a new session, so that the current
         * [HolderSessionState.Complete] result remains in place while the host navigates away.
         * Does nothing when the current session isn't complete, or when already dismissed.
         */
        fun dismiss()

        suspend fun confirmConsent()

        suspend fun denyConsent()

        companion object {
            const val JOURNEY_NAME: String = "holder"
        }
    }

    interface Verifier : Orchestrator {
        val verifierSessionState: StateFlow<VerifierSessionState>

        suspend fun processQrCode(qrCode: String?)

        companion object {
            const val JOURNEY_NAME: String = "verifier"
            val requiredPermissions = getBluetoothPermissions() + Manifest.permission.CAMERA
        }
    }

    /**
     * Property bag object containing logging messages common to [Orchestrator] implementations.
     */
    data object LogMessages {
        const val START_ORCHESTRATION_ERROR: String = "Cannot start orchestration"
        const val START_ORCHESTRATION_SUCCESS: String = "start orchestration"
        const val CANNOT_TRANSITION_TO_STATE: String = "Cannot transition " +
            "from current state: %s to state: %s"
        const val TRANSITION_SUCCESSFUL_TO_STATE: String = "Transition successful to state:"

        fun completedPrerequisiteChecks(journey: String, response: Any?): String =
            "Performed $journey prerequisite checks: $response"

        fun createSessionResetMessage(journey: String): String =
            "Cleared Orchestrator $journey session"

        fun recreateSessionOnStartMessage(journey: String): String =
            "Starting an Orchestrator $journey session after completing the previous journey"

        fun dismissedJourneyMessage(journey: String): String =
            "Dismissed the completed Orchestrator $journey journey"

        fun cannotDismissIncompleteJourneyMessage(journey: String): String =
            "Cannot dismiss the Orchestrator $journey journey as it hasn't completed"

        fun keepCompletedJourneyOnStartMessage(journey: String): String =
            "Keeping the completed Orchestrator $journey journey until it's dismissed"

        fun continueJourneyOnStartMessage(journey: String): String =
            "Continuing the in progress Orchestrator $journey journey"
    }
}
