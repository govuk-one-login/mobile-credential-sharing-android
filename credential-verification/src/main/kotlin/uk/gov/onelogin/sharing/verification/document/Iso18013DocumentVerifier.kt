@file:Suppress("UnusedParameter")

package uk.gov.onelogin.sharing.verification.document

import dev.zacsweers.metro.ContributesBinding
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.device.DeviceKeyInfo
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@ContributesBinding(CredentialVerificationScope::class)
class Iso18013DocumentVerifier(
    private val trustedRootCertificate: X509Certificate,
    private val trustVerifier: TrustVerifier
) : DocumentVerifier {
    override fun verifyDocument(
        document: VerifiableDocument,
        sessionTranscriptBytes: ByteArray?
    ): VerificationResult.Success {
        val (validityPeriod, encodedMSO) = trustVerifier.verifyCOSESign1(
            document.issuerSigned.issuerAuth,
            trustedRootCertificate
        )
        val mso = decodeMSO(encodedMSO)

        verifyMSOFields(document, mso)
        verifyDocumentDigests(document, mso)
        verifyValidityInfo(validityPeriod, mso)

        if (document is VerifiableDocument.WithPresentation) {
            if (sessionTranscriptBytes == null) {
                throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)
            }

            verifyDeviceAuth(document, sessionTranscriptBytes, mso.deviceKeyInfo)
            // move the proceeding call to `verifyDeviceAuth` during implementation.
            buildDeviceAuthenticationBytes(
                sessionTranscriptBytes,
                document.docType,
                document.deviceSigned.deviceNameSpacesBytes
            )
        }

        return VerificationResult.Success
    }

    /**
     * @throws VerificationResult.Failure
     */
    internal fun decodeMSO(encodedMSO: ByteArray): MobileSecurityObject =
        throw VerificationResult.Failure(VerificationError.MALFORMED_MSO)

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyMSOFields(document: VerifiableDocument, mso: MobileSecurityObject): Unit =
        throw VerificationResult.Failure(VerificationError.INVALID_MSO_VERSION)

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyDocumentDigests(
        document: VerifiableDocument,
        mso: MobileSecurityObject
    ): Unit = throw VerificationResult.Failure(VerificationError.DIGEST_MISMATCH)

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyValidityInfo(
        validityPeriod: CertificateValidityPeriod,
        mso: MobileSecurityObject
    ): Unit = throw VerificationResult.Failure(VerificationError.VALIDITY_SIGNED_OUT_OF_RANGE)

    /**
     * @throws VerificationResult.Failure
     */
    internal fun verifyDeviceAuth(
        document: VerifiableDocument.WithPresentation,
        sessionTranscriptBytes: ByteArray?,
        deviceKeyInfo: DeviceKeyInfo
    ): Unit = throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)

    internal fun buildDeviceAuthenticationBytes(
        sessionTranscriptBytes: ByteArray?,
        docType: String,
        deviceNameSpacesBytes: ByteArray
    ): ByteArray = byteArrayOf()
}
