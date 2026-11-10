package uk.gov.onelogin.sharing.orchestration.holder.session

/**
 * The information shown to the User on the engagement (QR code) screen.
 *
 * @param qrPayload The encoded Device Engagement QR payload to render, or `null` when the QR
 * must not be shown. The Orchestrator clears this during the renewal grace window so the expired
 * QR disappears while the engagement screen itself stays in place; the replacement session
 * supplies a fresh payload.
 * @param cancelRequiresConfirmation The cancellation rule for this screen:
 * - `false`: cancelling is permitted without confirmation
 * - `true`: cancelling is permitted after confirmation
 * - `null`: no cancel control is offered
 */
data class EngagementPresentation(
    val qrPayload: String?,
    val cancelRequiresConfirmation: Boolean? = false
) {
    /**
     * @return A copy of this presentation with the QR payload nullified, keeping the cancellation
     * rule.
     */
    fun withoutQr(): EngagementPresentation = copy(qrPayload = null)
}
