package uk.gov.onelogin.sharing.verification.reader

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope

@Inject
@ContributesBinding(AppScope::class)
@ContributesBinding(CredentialVerificationScope::class)
class ReaderAuthenticationImpl(
    private val verifyReaderAuthUseCase: VerifyReaderAuthUseCase,
    private val validatePrivacyPolicyUseCase: ValidatePrivacyPolicyUseCase
) : ReaderAuthentication {

    override fun authenticateDeviceRequest(
        deviceRequest: DeviceRequest,
        sessionTranscriptBytes: ByteArray,
        supportedDocumentTypes: List<String>,
        trustedReaderCertificates: List<X509Certificate>
    ): ReaderAuthenticationResult {
        var lastFailure: ReaderAuthenticationFailure? = null

        val supportedCandidates = deviceRequest.docRequests.filter {
            it.itemsRequest.docType in supportedDocumentTypes
        }

        val successOutcome = supportedCandidates.firstNotNullOfOrNull { candidateDocRequest ->
            try {
                val verifiedRequest = verifyReaderAuthUseCase.verify(
                    candidateDocRequest = candidateDocRequest,
                    untaggedSessionTranscriptBytes = sessionTranscriptBytes,
                    trustedReaderCertificates = trustedReaderCertificates
                )

                val authenticatedRequest = validatePrivacyPolicyUseCase.validate(verifiedRequest)

                ReaderAuthenticationResult.Success(authenticatedRequest)
            } catch (e: ReaderAuthenticationFailure) {
                lastFailure = e
                null
            }
        }

        if (successOutcome != null) {
            return successOutcome
        }

        lastFailure?.let { throw it }

        return ReaderAuthenticationResult.Unfulfillable
    }
}
