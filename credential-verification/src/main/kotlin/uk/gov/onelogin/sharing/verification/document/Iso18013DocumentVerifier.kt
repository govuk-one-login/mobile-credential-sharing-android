package uk.gov.onelogin.sharing.verification.document

import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript
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
        TODO("Not yet implemented")
    }
}
