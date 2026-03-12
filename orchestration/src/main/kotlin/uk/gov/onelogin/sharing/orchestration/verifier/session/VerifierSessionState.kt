package uk.gov.onelogin.sharing.orchestration.verifier.session

import uk.gov.onelogin.sharing.core.Completable
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionError

/**
 * Represents a digital credential verification journey's state for validating another device's
 * digital credentials.
 */
sealed class VerifierSessionState : Completable {

    override fun isComplete(): Boolean = this is Complete

    /**
     * Null-value object declaring that a User hasn't started a digital credential verification
     * journey yet.
     */
    data object NotStarted : VerifierSessionState()

    /**
     * State for when a User is ensuring all necessary steps to perform a digital credential
     * verification journey are complete.
     *
     * @param missingPrerequisites The list of permissions required to perform the journey in it's
     * entirety.
     */
    data class Preflight(val missingPrerequisites: Map<Prerequisite, PrerequisiteResponse>) :
        VerifierSessionState()

    /**
     * The User's completed the [Preflight] validations, so the device is ready to
     * scan a holder device's QR code.
     */
    data object ReadyToScan : VerifierSessionState()

    /**
     * State for when the Android-powered device is connecting with another device.
     *
     * The digital credential transfers between devices during this state.
     */
    data object Connecting : VerifierSessionState()

    /**
     * State for handling the Session engagement data obtained from a successfully connected holder
     * device.
     */
    data class ProcessingEngagement(val qrCode: String) : VerifierSessionState()

    /**
     * State for validating the successfully handled Session engagement data.
     */
    data object Verifying : VerifierSessionState()

    /**
     * State for when a User has finished a digital credential verification journey.
     */
    sealed class Complete(val reason: String) : VerifierSessionState() {
        /**
         * The User has completed a digital credential verification journey without un-resolvable
         * errors occurring.
         */
        data class Success(val data: DeviceResponse) : Complete("Successful journey")

        /**
         * The User cannot complete a digital credential verification journey due to encountering
         * an unresolvable error.
         */
        data class Failed(val error: SessionError) : Complete(error.message)

        /**
         * The User has chosen to stop a partially completed digital credential verification
         * journey.
         */
        data object Cancelled : Complete("Journey cancelled by User")
    }
}
