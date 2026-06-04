package uk.gov.onelogin.sharing.orchestration.verificationrequest

import java.security.cert.X509Certificate

data class VerifierConfig(
    val verificationRequest: VerificationRequest,
    val trustedRootCertificate: X509Certificate
)
