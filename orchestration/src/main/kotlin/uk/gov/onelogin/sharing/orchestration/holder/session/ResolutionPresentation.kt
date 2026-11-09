package uk.gov.onelogin.sharing.orchestration.holder.session

/**
 * User-facing failure buckets.
 *
 * Internal session errors are mapped to these for display; raw diagnostics are not surfaced.
 */
enum class FailureCategory {
    ConnectionInterrupted,
    RequestCannotBeUsed,
    CredentialUnavailable,
    SecureSharingCouldNotComplete,
    UnexpectedProblem
}

/**
 * The user-facing result of a resolved Holder journey.
 */
sealed interface ResolutionResult {
    /**
     * Confirmation awaiting dismissal: the holder's response was sent
     */
    data object DetailsShared : ResolutionResult

    /**
     * Error explanation awaiting dismissal: nothing matched the request
     */
    data object UnfulfillableRequest : ResolutionResult

    /**
     * Local error explanation awaiting dismissal for a terminal failure
     */
    data class Failed(val category: FailureCategory) : ResolutionResult
}

/**
 * Presentation payload for a resolved Holder journey.
 *
 * The UI selects localised text and success or error styling from [result] then calls
 * [dismissJourney] after user dismissal. A `null` [result] causes immediate dismissal.
 */
data class ResolutionPresentation(val result: ResolutionResult?) {
    companion object {

        fun from(successReason: HolderSessionState.Complete.SuccessReason) = when (successReason) {
            HolderSessionState.Complete.SuccessReason.Approved ->
                ResolutionPresentation(ResolutionResult.DetailsShared)

            HolderSessionState.Complete.SuccessReason.UnfulfillableRequest ->
                ResolutionPresentation(ResolutionResult.UnfulfillableRequest)

            HolderSessionState.Complete.SuccessReason.Denied ->
                ResolutionPresentation(result = null)
        }
    }
}
