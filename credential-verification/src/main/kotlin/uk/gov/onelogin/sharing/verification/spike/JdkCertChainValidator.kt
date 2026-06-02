package uk.gov.onelogin.sharing.verification.spike

import java.io.ByteArrayInputStream
import java.security.cert.CertPathValidator
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.PKIXCertPathChecker
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate

private const val ISSUER_ALT_NAME = "2.5.29.18"

object JdkCertChainValidator {

    fun validate(chainDerBytes: List<ByteArray>, trustedRoot: X509Certificate) {
        val certFactory = CertificateFactory.getInstance("X.509")
        val certs = chainDerBytes.map {
            certFactory.generateCertificate(ByteArrayInputStream(it)) as X509Certificate
        }
        val leaf = certs.first()

        // PKIX path validation + custom checker
        val certPath = certFactory.generateCertPath(certs)
        val params = PKIXParameters(setOf(TrustAnchor(trustedRoot, null))).apply {
            isRevocationEnabled = false
            addCertPathChecker(IssuerAltNameChecker(leaf))
        }
        CertPathValidator.getInstance("PKIX")
            .validate(certPath, params)
    }

    // Check IssuerAltName — must be present on leaf
    private class IssuerAltNameChecker(private val leaf: X509Certificate) : PKIXCertPathChecker() {
        override fun init(forward: Boolean) {}
        override fun isForwardCheckingSupported() = false
        override fun getSupportedExtensions(): Set<String>? = null
        override fun check(cert: Certificate, unresolvedCritExts: MutableCollection<String>?) {
            val x509 = cert as X509Certificate
            if (x509 == leaf) {
                requireNotNull(x509.getExtensionValue(ISSUER_ALT_NAME)) {
                    "IssuerAltName missing on leaf"
                }
            }
        }
    }
}
