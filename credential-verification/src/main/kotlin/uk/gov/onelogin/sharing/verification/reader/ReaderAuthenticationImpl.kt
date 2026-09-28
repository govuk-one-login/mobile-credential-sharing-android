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
    private val validatePrivacyPolicyUseCase: ValidatePrivacyPolicyUseCase
) : ReaderAuthentication {

    override fun authenticateDeviceRequest(
        deviceRequest: DeviceRequest,
        sessionTranscriptBytes: ByteArray,
        supportedDocumentTypes: List<String>,
        trustedReaderCertificates: List<X509Certificate>
    ): ReaderAuthenticationResult {
        val supportedCandidates = deviceRequest.docRequests.filter {
            it.itemsRequest.docType in supportedDocumentTypes
        }

        if (supportedCandidates.isEmpty()) return ReaderAuthenticationResult.Unfulfillable

        var lastFailure: ReaderAuthenticationFailure? = null

        for (candidate in supportedCandidates) {
            try {
                val verifiedRequest = verifyReaderAuthUseCase.verify(
                    candidateDocRequest = candidate,
                    untaggedSessionTranscriptBytes = sessionTranscriptBytes,
                    trustedReaderCertificates = trustedReaderCertificates
                )
                val authenticatedRequest = validatePrivacyPolicyUseCase.validate(verifiedRequest)
                return ReaderAuthenticationResult.Success(authenticatedRequest)
            } catch (e: ReaderAuthenticationFailure) {
                lastFailure = e
            }
        }

        throw lastFailure
            ?: ReaderAuthenticationFailure(ReaderAuthenticationReason.READER_AUTH_MISSING)
    }
}
