package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.core.Completable
import uk.gov.onelogin.sharing.core.UserCancellable
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction

/**
 * Represents a digital credential verification journey's state for devices that contain digital
 * credentials.
 */
sealed class HolderSessionState :
    Completable,
    UserCancellable {
    /**
     * @return `true` when the high-level journey is in an end state. Otherwise `false`.
     */
    override fun isComplete(): Boolean = this is Complete

    override fun userCanCancel(): Boolean = when (this) {
        is ProcessingEstablishment -> presentation.isCancellable
        is ProcessingResponse -> presentation.isCancellable
        is AwaitingVerifierResolution -> presentation.isCancellable
        else -> this::class in cancellableStates
    }
    override fun shouldConfirmCancellation(): Boolean = this::class in cancelConfirmationRequired
    override fun hideCloseButton(): Boolean = when (this) {
        is ProcessingEstablishment -> !presentation.showCloseButton
        is ProcessingResponse -> !presentation.showCloseButton
        is AwaitingVerifierResolution -> !presentation.showCloseButton
        else -> this::class in hiddenCloseButtonStates
    }

    /**
     * Whether a system back press should be intercepted and ignored for this state.
     *
     * This is the loading-screen states' counterpart to [userCanCancel]: when a loading phase is
     * not cancellable (for example "Sharing your details", where an in-flight transaction must not
     * be interrupted), the User must remain on the screen, so system back is swallowed rather than
     * navigating away.
     *
     * @return `true` when a loading phase is non-cancellable. Otherwise `false`.
     */
    fun blocksSystemBack(): Boolean = when (this) {
        is ProcessingEstablishment -> !presentation.isCancellable
        is ProcessingResponse -> !presentation.isCancellable
        is AwaitingVerifierResolution -> !presentation.isCancellable
        else -> false
    }

    /**
     * Null-value object declaring that a User hasn't started a digital credential verification
     * journey yet.
     */
    data object NotStarted : HolderSessionState()

    /**
     * State for when a User is ensuring all necessary steps to perform a digital credential
     * verification journey are complete.
     *
     * @param missingPrerequisites The list of [MissingPrerequisite]s required to perform the
     * journey in it's entirety.
     * @param onComplete The behaviour to call after a User action. Usually, this means performing
     * preflight checks again. Defaults to `{}`, meaning no additional behaviour occurs.
     */
    data class Preflight(
        val missingPrerequisites: List<MissingPrerequisite>,
        val onComplete: () -> Unit = {}
    ) : HolderSessionState(),
        Iterable<MissingPrerequisite> by missingPrerequisites {
        /**
         * Check if device's Bluetooth is turned off, and the user can enable it
         */
        fun requiresBluetoothEnabled(): Boolean = missingPrerequisites.any {
            it.prerequisite == Prerequisite.BLUETOOTH &&
                it.getAction() == PrerequisiteAction.EnableBluetooth
        }
    }

    /**
     * The User's completed the [Preflight] validations, so the device is ready to
     * present encoded engagement data for the verifying device.
     */
    data object ReadyToPresent : HolderSessionState()

    /**
     * The holder device is now showing encoded engagement data.
     *
     * @param qrData The encoded Device Engagement QR payload. Always present while this state is
     * active; the session is refreshed in place (new keys, new BLE UUID, new payload) at the
     * deadline, which emits a fresh [PresentingEngagement] via a new session.
     * @param showQr Whether the QR should be displayed. The orchestrator sets this to `false` for
     * the grace window shortly before the refresh deadline (the UI shows a loading indicator while
     * the current session keeps advertising), then a refreshed session emits a new
     * [PresentingEngagement] with `showQr = true`. This is a presentation hint carried on the
     * state; it is updated in place without a state transition.
     */
    data class PresentingEngagement(val qrData: String, val showQr: Boolean = true) :
        HolderSessionState()

    /**
     * State for when the Android-powered device is connected with another device and the
     * processing establishment begins.
     *
     * The digital credential transfers between devices during this state.
     *
     * @param presentation The loading screen presentation. Defaults to
     * [ProgressPresentation.GettingDetails] ("Getting list of details", cancellation available).
     */
    data class ProcessingEstablishment(
        val presentation: ProgressPresentation = ProgressPresentation.GettingDetails
    ) : HolderSessionState()

    /**
     * State for when a successful connection and processing establishment occurs,
     * allowing the User to consent to data being shared with the Verifying device.
     *
     * @param request The original decrypted [DeviceRequest]. Retained for building the
     * DeviceResponse on approval.
     * @param presentation The information shown on the consent screen:
     *    - The attributes plus the verified ReaderAuth privacy-policy link and organisation name.
     */
    data class AwaitingUserConsent(
        val request: DeviceRequest,
        val presentation: ConsentPresentation
    ) : HolderSessionState()

    /**
     * State for when the consenting User is generating the proof before completing the
     * Holder User journey.
     *
     * @param presentation The loading screen presentation. Defaults to
     * [ProgressPresentation.SharingDetails] ("Sharing your details", cancellation unavailable).
     */
    data class ProcessingResponse(
        val presentation: ProgressPresentation = ProgressPresentation.SharingDetails
    ) : HolderSessionState()

    /**
     * State for when the Holder has sent the DeviceResponse to the Verifier and is
     * awaiting the Verifier to close the session.
     *
     * Carries the same [ProgressPresentation.SharingDetails] as [ProcessingResponse] so the User
     * continues to see "Sharing your details" with cancellation unavailable; the screen does not
     * change when the response is sent.
     *
     * @param presentation The loading screen presentation. Defaults to
     * [ProgressPresentation.SharingDetails] ("Sharing your details", cancellation unavailable).
     */
    data class AwaitingVerifierResolution(
        val presentation: ProgressPresentation = ProgressPresentation.SharingDetails
    ) : HolderSessionState()

    /**
     * State for when the Holder has sent a SessionData(status: 20) termination message
     * and is executing the termination protocol (waiting for send completion then GATT End).
     * This is a transient state between sending the termination message and reaching
     * the terminal state.
     */
    data object SendingTermination : HolderSessionState()

    /**
     * State for when a User has finished a digital credential verification journey.
     */
    sealed class Complete(val reason: String) : HolderSessionState() {
        /**
         * The User has completed a digital credential verification journey without un-resolvable
         * errors occurring.
         */
        data class Success(val successReason: SuccessReason = SuccessReason.Approved) :
            Complete("Successful journey")

        /**
         * Describes why the journey completed successfully.
         */
        enum class SuccessReason {
            /** The user approved and the DeviceResponse was sent. */
            Approved,

            /** The user denied the request. */
            Denied,

            /** No matching document type, namespace, or attributes were found. */
            UnfulfillableRequest
        }

        /**
         * The User cannot complete a digital credential verification journey due to encountering
         * an unresolvable error.
         */
        data class Failed(val error: SessionError) : Complete(error.message) {
            val sessionReason: SessionErrorReason get() = error.reason
        }

        /**
         * The User has chosen to stop a partially completed digital credential verification
         * journey.
         */
        data object Cancelled : Complete("Journey cancelled by User")

        /**
         * The engagement session reached its deadline without a Verifier connecting and was
         * refreshed. The expired session is replaced by a new one carrying fresh keys, a new BLE
         * UUID, and a new QR payload. This terminal state records the expiry so that every created
         * session reaches a [Complete] state; it is not surfaced to the UI (the journey continues
         * on the replacement session).
         */
        data object Expired : Complete("Engagement session expired and was refreshed")
    }

    companion object {
        // Confirmation is still governed by this set. The loading-screen states
        // (ProcessingEstablishment, ProcessingResponse, AwaitingVerifierResolution) are included so
        // that cancelling them prompts for confirmation; their cancellability and close-button
        // visibility are driven by the ProgressPresentation they carry (see the overrides above).
        @JvmStatic
        private val cancelConfirmationRequired = listOf(
            AwaitingUserConsent::class,
            ProcessingEstablishment::class,
            ProcessingResponse::class,
            AwaitingVerifierResolution::class
        )

        // Cancellability for the loading-screen states is presentation-driven (see userCanCancel);
        // this set only covers the states that are not presentation-driven.
        @JvmStatic
        private val cancellableStates = listOf(
            Preflight::class,
            PresentingEngagement::class,
            AwaitingUserConsent::class
        )

        // Close-button visibility for the loading-screen states is presentation-driven (see
        // hideCloseButton); this set only covers the states that are not presentation-driven.
        @JvmStatic
        private val hiddenCloseButtonStates = listOf(
            AwaitingUserConsent::class
        )
    }
}
