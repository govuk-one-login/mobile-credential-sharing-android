package uk.gov.onelogin.sharing.orchestration.verifier.session

import uk.gov.onelogin.sharing.orchestration.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.VerifierConfig

object VerifierConfigStub {
    val verifierConfigStub = VerifierConfig(
        verificationRequest = VerificationRequest(
            documentType = "mdoc",
            requestedElements = emptyList()
        ),
        trustedCertificates = emptyList()
    )
}
