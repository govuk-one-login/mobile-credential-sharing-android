package uk.gov.onelogin.sharing.verification.reader

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest

/**
 * Production implementation of [ReaderAuthentication].
 *
 * Orchestrates candidate selection across candidates in a [DeviceRequest],
 * executing cryptographic verification and privacy policy URL validation.
 */
@Inject
@ContributesBinding(AppScope::class)
class ReaderAuthenticationImpl(
    private val verifyReaderAuthUseCase: VerifyReaderAuthUseCase,
    private val validatePrivacyPolicyUseCase: ValidatePrivacyPolicyUseCase,
    private val trustedReaderCertificates: List<X509Certificate>
) : ReaderAuthentication {

    override fun authenticateDeviceRequest(
        deviceRequest: DeviceRequest,
        untaggedSessionTranscriptBytes: ByteArray,
        supportedDocumentTypes: List<String>
    ): ReaderAuthenticationOutcome {
        var lastFailure: ReaderAuthenticationFailure? = null

        val supportedCandidates = deviceRequest.docRequests.filter {
            it.itemsRequest.docType in supportedDocumentTypes
        }

        val successOutcome = supportedCandidates.firstNotNullOfOrNull { candidateDocRequest ->
            try {
                val verifiedRequest = verifyReaderAuthUseCase.verify(
                    candidateDocRequest = candidateDocRequest,
                    untaggedSessionTranscriptBytes = untaggedSessionTranscriptBytes,
                    trustedReaderCertificates = this.trustedReaderCertificates
                )

                val authenticatedRequest = validatePrivacyPolicyUseCase.validate(verifiedRequest)

                ReaderAuthenticationOutcome.Success(authenticatedRequest)
            } catch (e: ReaderAuthenticationFailure) {
                lastFailure = e
                null
            }
        }

        if (successOutcome != null) {
            return successOutcome
        }

        lastFailure?.let { throw it }

        return ReaderAuthenticationOutcome.Unfulfillable
    }
}
