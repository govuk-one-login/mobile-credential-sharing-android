package uk.gov.onelogin.sharing.verification.spike

import java.security.cert.CertPathValidator
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.PKIXCertPathChecker
import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter

object BouncyCastleCertChainValidator {

    fun validate(chainDerBytes: List<ByteArray>, trustedRoot: X509Certificate) {
        val certs = chainDerBytes.map { X509CertificateHolder(it) }
        val leaf = certs.first()

        // PKIX path validation + custom checker
        val converter = JcaX509CertificateConverter()
        val certPath = CertificateFactory.getInstance("X.509")
            .generateCertPath(certs.map { converter.getCertificate(it) })

        val params = PKIXParameters(setOf(TrustAnchor(trustedRoot, null))).apply {
            isRevocationEnabled = false
            addCertPathChecker(IssuerAltNameChecker(leaf))
        }
        CertPathValidator.getInstance("PKIX", "BC")
            .validate(certPath, params)
    }

    // Check IssuerAltName — must be present on leaf
    private class IssuerAltNameChecker(private val leaf: X509CertificateHolder) :
        PKIXCertPathChecker() {
        override fun init(forward: Boolean) {}
        override fun isForwardCheckingSupported() = false
        override fun getSupportedExtensions(): Set<String>? = null
        override fun check(cert: Certificate, unresolvedCritExts: MutableCollection<String>?) {
            val x509 = cert as X509Certificate
            if (x509.encoded.contentEquals(leaf.encoded)) {
                requireNotNull(leaf.extensions.getExtension(Extension.issuerAlternativeName)) {
                    "IssuerAltName missing on leaf"
                }
            }
        }
    }
}
