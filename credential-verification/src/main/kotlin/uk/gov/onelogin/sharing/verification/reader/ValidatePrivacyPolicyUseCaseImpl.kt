package uk.gov.onelogin.sharing.verification.reader

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationReason.PRIVACY_POLICY_URL_INVALID

/**
 * Implementation of [ValidatePrivacyPolicyUseCase].
 *
 * Extracts and validates the DVS Privacy Policy URL from the Subject Information Access (SIA) extension
 * of the verified Reader leaf certificate, extracts the unvalidated organizationName, and discards the certificate.
 */
@Inject
@ContributesBinding(CredentialVerificationScope::class)
@ContributesBinding(AppScope::class)
class ValidatePrivacyPolicyUseCaseImpl(
    private val siaExtensionParser: SiaExtensionParser,
    private val privacyPolicyUrlValidator: PrivacyPolicyUrlValidator,
    private val subjectNameParser: SubjectNameParser
) : ValidatePrivacyPolicyUseCase {

    override fun validate(
        verifiedReaderRequest: VerifiedReaderRequest
    ): AuthenticatedReaderRequest {
        val leafCert = verifiedReaderRequest.readerCertificate

        val rawUrl = siaExtensionParser.extractPrivacyPolicyUrl(leafCert)
            ?: throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)
        val validUri = privacyPolicyUrlValidator.validate(rawUrl)
            ?: throw ReaderAuthenticationFailure(PRIVACY_POLICY_URL_INVALID)

        return AuthenticatedReaderRequest(
            docRequest = verifiedReaderRequest.docRequest,
            privacyPolicyUrl = validUri,
            readerOrganizationName = subjectNameParser.extractOrganizationName(leafCert)
        )
    }
}
