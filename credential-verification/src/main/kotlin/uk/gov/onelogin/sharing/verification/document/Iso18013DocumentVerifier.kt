package uk.gov.onelogin.sharing.verification.document

import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript
import uk.gov.onelogin.sharing.verification.document.models.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.document.models.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.document.models.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

class Iso18013DocumentVerifier(
    private val trustedRootCertificate: X509Certificate,
    private val trustVerifier: TrustVerifier,
) : DocumentVerifier {
    override fun verifyDocument(
        document: VerifiableDocument,
        transcript: SessionTranscript?,
    ): VerificationResult.Success {
        val (certificateValidity, data) = trustVerifier.verifyCOSESign1(
            document.issuerSigned.issuerAuth,
            trustedRootCertificate
        )
        val mso = decodeMSO(data)

        return VerificationResult.Success
    }

    /**
     * @throws VerificationResult.Failure
     */
    internal fun decodeMSO(encodedMSO: ByteArray): MobileSecurityObject {
        throw VerificationResult.Failure(VerificationError.MALFORMED_MSO)
    }

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyMSOFields(document: VerifiableDocument, mso: MobileSecurityObject) {
        throw VerificationResult.Failure(VerificationError.INVALID_MSO_VERSION)
    }

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyDocumentDigests(document: VerifiableDocument, mso: MobileSecurityObject) {
        throw VerificationResult.Failure(VerificationError.DIGEST_MISMATCH)
    }

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyValidityInfo(
        validityPeriod: CertificateValidityPeriod,
        mso: MobileSecurityObject
    ) {
        throw VerificationResult.Failure(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE)
    }

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyDeviceAuth(
        document: VerifiableDocument.WithPresentation,
        sessionTranscript: SessionTranscript,
        deviceKeyInfo: DeviceKeyInfo
    ) {
        throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)
    }

    internal fun buildDeviceAuthenticationBytes(
        sessionTranscript: SessionTranscript,
        docType: String,
        deviceNameSpacesBytes: ByteArray
    ): ByteArray = byteArrayOf()
}
