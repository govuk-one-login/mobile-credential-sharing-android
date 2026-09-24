package uk.gov.onelogin.sharing.verification.reader

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ReaderAuthenticationDto
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationRequest
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationResult
import uk.gov.onelogin.sharing.verification.cose.CoseVerifier
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationReason.INVALID_READER_SIGNATURE
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationReason.MALFORMED_READER_AUTH
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationReason.READER_AUTH_MISSING
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationReason.UNSUPPORTED_READER_AUTH_ALGORITHM
import uk.gov.onelogin.sharing.verification.reader.ReaderAuthenticationReason.UNTRUSTED_READER_CERTIFICATE

/**
 * Production implementation of [VerifyReaderAuthUseCase].
 *
 * Reconstructs the exact [ReaderAuthenticationBytes] payload from the active untagged session transcript
 * and preserved [DocRequest.itemsRequestBytes], and executes certificate-backed COSE_Sign1 signature verification.
 */
@Inject
@ContributesBinding(AppScope::class)
@ContributesBinding(CredentialVerificationScope::class)
class VerifyReaderAuthUseCaseImpl(private val coseVerifier: CoseVerifier) :
    VerifyReaderAuthUseCase {

    override fun verify(
        candidateDocRequest: DocRequest,
        untaggedSessionTranscriptBytes: ByteArray,
        trustedReaderCertificates: List<X509Certificate>
    ): VerifiedReaderRequest {
        val rawReaderAuth = candidateDocRequest.readerAuth
            ?: throw ReaderAuthenticationFailure(READER_AUTH_MISSING)

        val itemsRequestBytes = candidateDocRequest.itemsRequestBytes
            ?: throw ReaderAuthenticationFailure(MALFORMED_READER_AUTH)

        val detachedPayload = try {
            ReaderAuthenticationDto.createReaderAuthenticationBytes(
                untaggedSessionTranscriptBytes = untaggedSessionTranscriptBytes,
                itemsRequestBytes = itemsRequestBytes
            )
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            throw ReaderAuthenticationFailure(MALFORMED_READER_AUTH, e)
        }

        val coseRequest = CoseVerificationRequest.Detached(
            coseSign1Bytes = rawReaderAuth,
            detachedPayload = detachedPayload,
            trustedRoots = trustedReaderCertificates
        )

        val result = try {
            coseVerifier.verify(coseRequest) as CoseVerificationResult.Detached
        } catch (e: CoseVerificationFailure) {
            throw ReaderAuthenticationFailure(mapCoseFailureReason(e), e)
        }

        return VerifiedReaderRequest(
            docRequest = candidateDocRequest,
            readerCertificate = result.leafCertificate
        )
    }

    private fun mapCoseFailureReason(failure: CoseVerificationFailure): ReaderAuthenticationReason =
        when (failure) {
            is CoseVerificationFailure.InvalidSignature -> INVALID_READER_SIGNATURE

            is CoseVerificationFailure.MalformedCoseSign1,
            is CoseVerificationFailure.MissingX5Chain -> MALFORMED_READER_AUTH

            is CoseVerificationFailure.UnsupportedAlgorithm -> UNSUPPORTED_READER_AUTH_ALGORITHM

            is CoseVerificationFailure.UntrustedCertificate,
            is CoseVerificationFailure.ExpiredCertificate,
            is CoseVerificationFailure.CertificateProfileViolation -> UNTRUSTED_READER_CERTIFICATE
        }
}
