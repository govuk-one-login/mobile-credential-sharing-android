package uk.gov.onelogin.sharing.orchestration.session

import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument

/**
 * Holds the verified document data from a completed Verifier journey.
 */
data class DeviceResponse(val documents: List<VerifiableDocument.WithPresentation> = emptyList())
