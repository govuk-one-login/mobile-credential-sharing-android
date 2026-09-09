package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.X509Certificate

internal fun interface CertificateChainValidator {
    /**
     * Validates a certificate chain against a trusted root.
     *
     * @param certificates The ordered chain of certificates, starting with the leaf.
     * @param trustedRoot The root certificate to anchor the trust.
     */
    fun verify(certificates: List<X509Certificate>, trustedRoot: X509Certificate)
}
