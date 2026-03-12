package uk.gov.onelogin.sharing.orchestration.verifier.session

import uk.gov.onelogin.VerificationRequest
import uk.gov.onelogin.VerifierConfig

object VerifierConfigStub {
    val verifierConfigStub = VerifierConfig(
        verificationRequest = VerificationRequest(
            documentType = "mdoc",
            requestedElements = emptyList()
        ),
        trustedCertificates = emptyList()
    )
}
