package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.verification.reader.AuthenticatedReaderRequest

/**
 * Builds an [AuthenticatedReaderRequest] for a candidate [DocRequest].
 *
 * Extracting this keeps the Android [android.net.Uri] dependency out of
 * [uk.gov.onelogin.sharing.orchestration.HolderOrchestrator], so the orchestrator can be unit
 * tested without static Uri mocking.
 *
 * To be removed in: https://govukverify.atlassian.net/browse/DCMAW-23451 (EX2)
 */
fun interface AuthenticatedReaderRequestFactory {
    fun create(docRequest: DocRequest): AuthenticatedReaderRequest
}
