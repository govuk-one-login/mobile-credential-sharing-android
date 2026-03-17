package uk.gov.onelogin.sharing.orchestration

import java.security.cert.Certificate

data class VerifierConfig(
    val verificationRequest: VerificationRequest,
    val trustedCertificates: List<Certificate>
)

data class VerificationRequest(val documentType: String, val requestedElements: List<String>)
