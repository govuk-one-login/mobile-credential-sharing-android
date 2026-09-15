package uk.gov.onelogin.sharing.verification.trust

import dev.zacsweers.metro.ContributesBinding
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinInstant
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationRequest
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationResult
import uk.gov.onelogin.sharing.verification.cose.CoseVerifierImpl
import uk.gov.onelogin.sharing.verification.cose.internal.path.OID_COUNTRY
import uk.gov.onelogin.sharing.verification.cose.internal.path.OID_STATE_OR_PROVINCE
import uk.gov.onelogin.sharing.verification.cose.internal.path.parseSubjectName
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.format.document.validity.IssuerAuthResult

@ContributesBinding(CredentialVerificationScope::class)
class TrustVerifierImpl internal constructor(private val coseVerifier: CoseVerifierImpl) :
    TrustVerifier {

    @OptIn(ExperimentalTime::class)
    override fun verifyCOSESign1(data: ByteArray, trustedRoot: X509Certificate): IssuerAuthResult =
        try {
            val result = coseVerifier.verify(
                CoseVerificationRequest.Attached(data, trustedRoot)
            ) as CoseVerificationResult.Attached

            val leaf = result.leafCertificate
            val subjectName = parseSubjectName(leaf)

            IssuerAuthResult(
                certificateValidityPeriod = CertificateValidityPeriod(
                    notBefore = leaf.notBefore.toInstant().toKotlinInstant(),
                    notAfter = leaf.notAfter.toInstant().toKotlinInstant()
                ),
                msoPayload = result.payload,
                subjectCountry = subjectName[OID_COUNTRY]
                    ?: throw CoseVerificationFailure.MalformedCoseSign1,
                subjectState = subjectName[OID_STATE_OR_PROVINCE]
            )
        } catch (e: CoseVerificationFailure) {
            throw mapCoseFailure(e, isIssuer = true)
        }

    override fun verifyCOSESign1(coseData: ByteArray, publicKey: ECPublicKey, payload: ByteArray) {
        try {
            coseVerifier.verify(
                CoseVerificationRequest.KeyBased(
                    coseSign1Bytes = coseData,
                    detachedPayload = payload,
                    publicKey = publicKey
                )
            )
        } catch (e: CoseVerificationFailure) {
            throw mapCoseFailure(e, isIssuer = false)
        }
    }

    private fun mapCoseFailure(
        e: CoseVerificationFailure,
        isIssuer: Boolean
    ): VerificationResult.Failure {
        val error = when (e) {
            is CoseVerificationFailure.MalformedCoseSign1,
            is CoseVerificationFailure.MissingX5Chain ->
                if (isIssuer) {
                    VerificationError.MALFORMED_ISSUER_AUTH
                } else {
                    VerificationError.INVALID_DEVICE_SIGNATURE
                }

            is CoseVerificationFailure.UnsupportedAlgorithm,
            is CoseVerificationFailure.InvalidSignature ->
                if (isIssuer) {
                    VerificationError.INVALID_ISSUER_SIGNATURE
                } else {
                    VerificationError.INVALID_DEVICE_SIGNATURE
                }

            is CoseVerificationFailure.UntrustedCertificate,
            is CoseVerificationFailure.CertificateProfileViolation ->
                VerificationError.UNTRUSTED_CERTIFICATE

            is CoseVerificationFailure.ExpiredCertificate ->
                VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE
        }
        return VerificationResult.Failure(error)
    }
}
