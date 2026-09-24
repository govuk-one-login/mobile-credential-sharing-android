package uk.gov.onelogin.sharing.orchestration.holder.session

import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.verification.reader.AuthenticatedReaderRequest

/**
 * Temporary implementation that returns placeholder privacy-policy and organisation values.
 *
 * Used on the deprecated path.
 *
 * To be removed in: https://govukverify.atlassian.net/browse/DCMAW-23451 (EX2)
 */
@Inject
@ContributesBinding(scope = AppScope::class, binding = binding<AuthenticatedReaderRequestFactory>())
class DummyAuthenticatedReaderRequestFactory : AuthenticatedReaderRequestFactory {
    override fun create(docRequest: DocRequest): AuthenticatedReaderRequest =
        AuthenticatedReaderRequest(
            docRequest = docRequest,
            privacyPolicyUrl = "https://gov.uk/".toUri(),
            readerOrganizationName = "Example Organisation Name"
        )
}
