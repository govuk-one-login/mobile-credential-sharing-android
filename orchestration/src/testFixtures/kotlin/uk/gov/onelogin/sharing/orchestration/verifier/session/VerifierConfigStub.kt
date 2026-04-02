package uk.gov.onelogin.sharing.orchestration.verifier.session

import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig

object VerifierConfigStub {
    val verifierConfigStub = VerifierConfig(
        verificationRequest = VerificationRequest(
            documentType = "mdoc",
            attributeGroup = AttributeGroup(emptyMap())
        ),
        trustedCertificates = emptyList()
    )
}
