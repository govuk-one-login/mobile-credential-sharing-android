package uk.gov.onelogin.sharing.orchestration.holder.session

import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.verification.reader.AuthenticatedReaderRequest

/**
 * Test double for [AuthenticatedReaderRequestFactory].
 *
 * [android.net.Uri] is the throwing Android stub in plain JVM unit tests, so the default
 * [privacyPolicyUrl] is a mock whose `toString()` returns [DEFAULT_PRIVACY_POLICY_URL]. This keeps
 * the Uri handling contained in the fake, out of both production code and the test classes.
 *
 * To be removed in: https://govukverify.atlassian.net/browse/DCMAW-23451 (EX2)
 */
class FakeAuthenticatedReaderRequestFactory(
    private val privacyPolicyUrl: Uri = mockk {
        every { this@mockk.toString() } returns DEFAULT_PRIVACY_POLICY_URL
    },
    private val readerOrganizationName: String? = DEFAULT_ORGANISATION_NAME
) : AuthenticatedReaderRequestFactory {
    override fun create(docRequest: DocRequest): AuthenticatedReaderRequest =
        AuthenticatedReaderRequest(
            docRequest = docRequest,
            privacyPolicyUrl = privacyPolicyUrl,
            readerOrganizationName = readerOrganizationName
        )

    companion object {
        const val DEFAULT_PRIVACY_POLICY_URL = "https://gov.uk/"
        const val DEFAULT_ORGANISATION_NAME = "Example Organisation Name"
    }
}
