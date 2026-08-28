package uk.gov.onelogin.sharing.verification.trust

import dev.zacsweers.metro.ContributesBinding
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinInstant
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseSign1Decoder
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateChainValidator
import uk.gov.onelogin.sharing.verification.cose.internal.path.OID_COUNTRY
import uk.gov.onelogin.sharing.verification.cose.internal.path.OID_STATE_OR_PROVINCE
import uk.gov.onelogin.sharing.verification.cose.internal.path.orderCertificates
import uk.gov.onelogin.sharing.verification.cose.internal.path.parseSubjectName
import uk.gov.onelogin.sharing.verification.cose.internal.signature.CoseSignatureVerifier
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.format.document.validity.IssuerAuthResult

@ContributesBinding(CredentialVerificationScope::class)
class TrustVerifierImpl internal constructor(
    private val coseSign1Decoder: CoseSign1Decoder,
    private val signatureVerifier: CoseSignatureVerifier,
    private val certificateChainValidator: CertificateChainValidator
) : TrustVerifier {

    @OptIn(ExperimentalTime::class)
    override fun verifyCOSESign1(data: ByteArray, trustedRoot: X509Certificate): IssuerAuthResult = try {
        val coseSign1 = coseSign1Decoder.decode(data)
        val x5chain = coseSign1Decoder.extractX5Chain(coseSign1)

        val certFactory = CertificateFactory.getInstance("X.509")
        val certs = x5chain.map {
            certFactory.generateCertificate(ByteArrayInputStream(it)) as X509Certificate
        }

        val ordered = orderCertificates(certs)
        val leaf = ordered.first()

        certificateChainValidator.verify(ordered, trustedRoot)

        val publicKey = try {
            leaf.publicKey as ECPublicKey
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            throw CoseVerificationFailure.UntrustedCertificate
        }

        val payload = coseSign1.payload
            ?: throw CoseVerificationFailure.MalformedCoseSign1

        signatureVerifier.verify(
            coseSign1,
            publicKey,
            payload
        )

        val validityPeriod = CertificateValidityPeriod(
            notBefore = leaf.notBefore.toInstant().toKotlinInstant(),
            notAfter = leaf.notAfter.toInstant().toKotlinInstant()
        )

        val subjectName = parseSubjectName(leaf)

        IssuerAuthResult(
            certificateValidityPeriod = validityPeriod,
            msoPayload = payload,
            subjectCountry = subjectName[OID_COUNTRY]
                ?: throw CoseVerificationFailure.MalformedCoseSign1,
            subjectState = subjectName[OID_STATE_OR_PROVINCE]
        )
    } catch (e: CoseVerificationFailure) {
        throw mapCoseFailure(e, isIssuer = true)
    }

    override fun verifyCOSESign1(coseData: ByteArray, publicKey: ECPublicKey, payload: ByteArray) {
        try {
            val coseSign1 = coseSign1Decoder.decode(coseData)
            signatureVerifier.verify(
                coseSign1,
                publicKey,
                payload
            )
        } catch (e: CoseVerificationFailure) {
            throw mapCoseFailure(e, isIssuer = false)
        }
    }

    private fun mapCoseFailure(e: CoseVerificationFailure, isIssuer: Boolean): VerificationResult.Failure {
        val error = when (e) {
            is CoseVerificationFailure.MalformedCoseSign1 ->
                if (isIssuer) VerificationError.MALFORMED_ISSUER_AUTH else VerificationError.INVALID_DEVICE_SIGNATURE

            is CoseVerificationFailure.UnsupportedAlgorithm,
            is CoseVerificationFailure.InvalidSignature ->
                if (isIssuer) VerificationError.INVALID_ISSUER_SIGNATURE else VerificationError.INVALID_DEVICE_SIGNATURE

            is CoseVerificationFailure.UntrustedCertificate,
            is CoseVerificationFailure.UnsupportedCertificateProfile -> VerificationError.UNTRUSTED_CERTIFICATE

            is CoseVerificationFailure.ExpiredCertificate -> VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE
        }
        return VerificationResult.Failure(error)
    }
}
