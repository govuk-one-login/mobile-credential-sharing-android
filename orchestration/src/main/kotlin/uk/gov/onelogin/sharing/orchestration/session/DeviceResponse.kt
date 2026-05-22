package uk.gov.onelogin.sharing.orchestration.session

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document

/**
 * Holds the verified document data from a completed Verifier journey.
 */
data class DeviceResponse(val documents: List<Document> = emptyList())
