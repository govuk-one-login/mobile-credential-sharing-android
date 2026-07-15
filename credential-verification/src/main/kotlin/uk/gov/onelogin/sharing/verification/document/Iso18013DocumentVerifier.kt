@file:Suppress("UnusedParameter")

package uk.gov.onelogin.sharing.verification.document

import dev.zacsweers.metro.ContributesBinding
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.verification.CredentialVerificationScope
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocument
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier

@Suppress("LongParameterList")
@ContributesBinding(CredentialVerificationScope::class)
class Iso18013DocumentVerifier(
    private val trustedRootCertificate: X509Certificate,
    private val trustVerifier: TrustVerifier,
    private val deviceAuthVerifier: DeviceAuthVerifier,
    private val msoDecoder: MsoDecoder,
    private val msoFieldVerifier: MsoFieldVerifier,
    private val validityInfoVerifier: ValidityInfoVerifier,
    private val digestVerifier: DigestVerifier
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

        msoFieldVerifier.verify(document, mso, issuerAuthResult)
        validityInfoVerifier.verify(
            issuerAuthResult.certificateValidityPeriod,
            mso.validityInfo
        )
        digestVerifier.verify(document, mso)

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
}
