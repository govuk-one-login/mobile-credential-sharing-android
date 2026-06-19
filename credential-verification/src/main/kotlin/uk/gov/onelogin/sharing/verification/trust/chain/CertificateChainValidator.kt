package uk.gov.onelogin.sharing.verification.trust.chain

import java.security.cert.X509Certificate

interface CertificateChainValidator {
    fun verify(certificates: List<X509Certificate>, trustedRoot: X509Certificate)
}
