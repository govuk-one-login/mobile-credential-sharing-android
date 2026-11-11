package uk.gov.onelogin.sharing.holder.presentation

import uk.gov.onelogin.sharing.orchestration.holder.session.EngagementPresentation

/**
 * What the QR code screen renders.
 *
 * Built from the Orchestrator's [EngagementPresentation] so that UI doesn't depend on
 * orchestration types.
 *
 * @param qrPayload The QR payload to render, or `null` to hide the QR code
 */
data class QrCodeUiState(val qrPayload: String? = null) {
    constructor(presentation: EngagementPresentation) : this(qrPayload = presentation.qrPayload)
}
