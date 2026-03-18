package uk.gov.onelogin.sharing.orchestration.verificationrequest

import java.security.cert.Certificate

data class VerifierConfig(
    val verificationRequest: VerificationRequest,
    val trustedCertificates: List<Certificate>
)
