package uk.gov.onelogin.sharing.orchestration.holder.session

/**
 * The information shown to the User on a transitional loading screen.
 *
 * Carried on the transitional [HolderSessionState]s (for example
 * [HolderSessionState.ProcessingEstablishment], [HolderSessionState.ProcessingResponse] and
 * [HolderSessionState.AwaitingVerifierResolution]), following the same convention as
 * [ConsentPresentation]: the Orchestrator owns the presentation and the UI simply renders it.
 *
 * This is the single rule the loading-screen states follow for both Back and Close:
 * [isCancellable] drives whether the User can back out (system back / future secondary button) and
 * [showCloseButton] drives whether the toolbar close icon is shown. Keeping both on the presentation
 * means a new or changed phase only needs a new preset here; the [HolderSessionState] logic does not
 * change.
 *
 * Carrying [Phase.SharingDetails] on both [HolderSessionState.ProcessingResponse] and
 * [HolderSessionState.AwaitingVerifierResolution] lets the Holder move from sending the response to
 * waiting for the Verifier without changing what the User sees.
 *
 * @param phase Which loading phase is in progress. Drives the displayed label and the GA4 identity.
 * @param isCancellable Whether the User may cancel (system back, and in future a secondary "back"
 * button) during this phase.
 * @param showCloseButton Whether the toolbar close icon is shown during this phase. Independent of
 * [isCancellable]: a phase can be cancellable while still hiding the close icon.
 */
data class ProgressPresentation(
    val phase: Phase,
    val isCancellable: Boolean,
    val showCloseButton: Boolean
) {
    /**
     * The loading phase currently in progress.
     */
    enum class Phase {
        /** The device is establishing the connection and getting the list of requested details. */
        GettingDetails,

        /** The device is sharing the User's details and awaiting the Verifier to resolve. */
        SharingDetails
    }

    companion object {
        /**
         * "Getting list of details" — cancellable with the close icon shown while the connection is
         * established and the request is processed.
         */
        val GettingDetails = ProgressPresentation(
            phase = Phase.GettingDetails,
            isCancellable = true,
            showCloseButton = true
        )

        /**
         * "Sharing your details" — no cancel behaviour: the User cannot back out and the close
         * icon is hidden. Used from sending the response through to awaiting the Verifier's
         * resolution so the screen does not change on send and an in-flight transaction cannot be
         * cancelled.
         */
        val SharingDetails = ProgressPresentation(
            phase = Phase.SharingDetails,
            isCancellable = false,
            showCloseButton = false
        )
    }
}
