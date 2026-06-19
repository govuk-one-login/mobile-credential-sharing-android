package uk.gov.onelogin.sharing.verification.trust.chain

import java.security.cert.X509Certificate

/**
 * Validates an X.509 certificate chain.
 */
interface CertificateChainValidator {
    /**
     * Verifies that certificates form a valid chain anchored to trustedRoot.
     *
     * @throws
     * [uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult.Failure]
     */
    fun verify(certificates: List<X509Certificate>, trustedRoot: X509Certificate)
}
