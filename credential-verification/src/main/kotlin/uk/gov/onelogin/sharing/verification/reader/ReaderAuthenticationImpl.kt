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
 * executing R4 cryptographic verification and R5 privacy policy URL validation.
 */
@Inject
@ContributesBinding(AppScope::class)
class ReaderAuthenticationImpl(
    private val verifyReaderAuthUseCase: VerifyReaderAuthUseCase,
    private val validatePrivacyPolicyUseCase: ValidatePrivacyPolicyUseCase,
) : ReaderAuthentication {

    override fun authenticateDeviceRequest(
        deviceRequest: DeviceRequest,
        untaggedSessionTranscriptBytes: ByteArray,
        trustedReaderCertificates: List<X509Certificate>,
    ): ReaderAuthenticationOutcome {
        var lastFailure: ReaderAuthenticationFailure? = null

        for (candidateDocRequest in deviceRequest.docRequests) {
            try {
                val verifiedRequest = verifyReaderAuthUseCase.verify(
                    candidateDocRequest = candidateDocRequest,
                    untaggedSessionTranscriptBytes = untaggedSessionTranscriptBytes,
                    trustedReaderCertificates = trustedReaderCertificates,
                )

                val authenticatedRequest = validatePrivacyPolicyUseCase.validate(verifiedRequest)

                return ReaderAuthenticationOutcome.Success(authenticatedRequest)
            } catch (e: ReaderAuthenticationFailure) {
                lastFailure = e
            }
        }

        lastFailure?.let { throw it }

        return ReaderAuthenticationOutcome.Unfulfillable
    }
}
