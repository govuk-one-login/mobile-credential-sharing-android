package uk.gov.onelogin.sharing.di.api.verifier

import java.security.cert.Certificate

fun interface VerifierCredentialSdk {
    fun verifier(
        verificationRequest: VerificationRequest,
        trustedCertificates: List<Certificate>
    ): CredentialVerifier
}
