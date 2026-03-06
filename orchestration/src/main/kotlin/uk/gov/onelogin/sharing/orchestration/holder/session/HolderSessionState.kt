package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.core.Completable
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionError

/**
 * Represents a digital credential verification journey's state for devices that contain digital
 * credentials.
 */
sealed class HolderSessionState : Completable {
    /**
     * @return `true` when the high-level journey is in an end state. Otherwise `false`.
     */
    override fun isComplete(): Boolean = this is Complete

    /**
     * Null-value object declaring that a User hasn't started a digital credential verification
     * journey yet.
     */
    data object NotStarted : HolderSessionState()

    /**
     * State for when a User is ensuring all necessary steps to perform a digital credential
     * verification journey are complete.
     *
     * @param missingPrerequisites The list of [Prerequisite]s required to perform the journey in
     * it's entirety.
     */
    data class Preflight(val missingPrerequisites: Map<Prerequisite, PrerequisiteResponse>) :
        HolderSessionState()

    /**
     * The User's completed the [Preflight] validations, so the device is ready to
     * present encoded engagement data for the verifying device.
     */
    data object ReadyToPresent : HolderSessionState()

    /**
     * The holder device is now showing encoded engagement data.
     */
    data class PresentingEngagement(val qrData: String) : HolderSessionState()

    /**
     * State for when the Android-powered device is connected with another device and the
     * processing establishment begins.
     *
     * The digital credential transfers between devices during this state.
     */
    data object ProcessingEstablishment : HolderSessionState()

    /**
     * State for when a successful connection and processing establishment occurs,
     * allowing the User to consent to data being shared with the Verifying device.
     */
    data class AwaitingUserConsent(val request: DeviceRequest) : HolderSessionState()

    /**
     * State for when the consenting User is generating the proof before completing the
     * Holder User journey.
     */
    data object ProcessingResponse : HolderSessionState()

    /**
     * State for when a User has finished a digital credential verification journey.
     */
    sealed class Complete(val reason: String) : HolderSessionState() {
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
