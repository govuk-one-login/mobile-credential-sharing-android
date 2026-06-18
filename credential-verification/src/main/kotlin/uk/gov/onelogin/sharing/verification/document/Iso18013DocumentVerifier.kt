@file:Suppress("UnusedParameter")

package uk.gov.onelogin.sharing.verification.document

import dev.zacsweers.metro.ContributesBinding
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.MobileSecurityObject
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.validity.CertificateValidityPeriod
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@ContributesBinding(CredentialVerificationScope::class)
class Iso18013DocumentVerifier(
    private val trustedRootCertificate: X509Certificate,
    private val trustVerifier: TrustVerifier,
    private val deviceAuthVerifier: DeviceAuthVerifier,
    private val msoDecoder: MsoDecoder,
    private val verifyMsoFields: VerifyMsoFieldsUseCase
) : DocumentVerifier {

    override fun verifyDocument(
        document: VerifiableDocument,
        sessionTranscriptBytes: ByteArray?
    ): VerificationResult.Success {
        val issuerAuthResult = trustVerifier.verifyCOSESign1(
            document.issuerSigned.issuerAuth,
            trustedRootCertificate
        )
        val mso = msoDecoder.decode(issuerAuthResult.msoPayload)

        verifyMsoFields.verify(document, mso, issuerAuthResult)
        verifyDocumentDigests(document, mso)
        verifyValidityInfo(issuerAuthResult.certificateValidityPeriod, mso)

        if (document is VerifiableDocument.WithPresentation) {
            if (sessionTranscriptBytes == null) {
                throw VerificationResult.Failure(VerificationError.INVALID_DEVICE_SIGNATURE)
            }

            deviceAuthVerifier.verify(
                document = document,
                sessionTranscriptBytes = sessionTranscriptBytes,
                deviceKeyInfo = mso.deviceKeyInfo
            )
        }

        return VerificationResult.Success
    }

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
}
