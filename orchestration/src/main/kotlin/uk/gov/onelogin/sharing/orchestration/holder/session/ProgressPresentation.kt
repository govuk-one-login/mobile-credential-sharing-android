package uk.gov.onelogin.sharing.orchestration.holder.session

/**
 * The information shown to the User on a transitional loading screen.
 *
 * Carried on the transitional [HolderSessionState]s (for example
 * [HolderSessionState.ProcessingEstablishment], [HolderSessionState.ProcessingResponse] and
 * [HolderSessionState.AwaitingVerifierResolution]), following the same convention as
 * [ConsentPresentation]: the Orchestrator owns the presentation and the UI simply renders it.
 *
 * Driving the loading screen from this object gives Back and Close a single cancellation rule to
 * follow ([cancellationAvailable]) and lets the Holder move from sending the response
 * ([Phase.SharingDetails] on [HolderSessionState.ProcessingResponse]) to waiting for the Verifier
 * ([Phase.SharingDetails] on [HolderSessionState.AwaitingVerifierResolution]) without changing what
 * the User sees.
 *
 * @param phase Which loading phase is in progress. Drives the displayed label and the GA4 identity.
 * @param cancellationAvailable Whether the User may cancel (Back / Close) during this phase. This is
 * the single source of truth for cancellation while a loading screen is shown.
 */
data class ProgressPresentation(
    val phase: Phase,
    val cancellationAvailable: Boolean
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
         * "Getting list of details" — cancellation is available (with confirmation) while the
         * connection is established and the request is processed.
         */
        val GettingDetails = ProgressPresentation(
            phase = Phase.GettingDetails,
            cancellationAvailable = true
        )

        /**
         * "Sharing your details" — cancellation is unavailable. Used from sending the response
         * through to awaiting the Verifier's resolution so the screen does not change on send and
         * the User cannot cancel a transaction that is already in flight.
         */
        val SharingDetails = ProgressPresentation(
            phase = Phase.SharingDetails,
            cancellationAvailable = false
        )
    }
}
