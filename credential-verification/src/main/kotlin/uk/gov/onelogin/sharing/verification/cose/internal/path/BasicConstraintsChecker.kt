package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.PKIXCertPathChecker
import java.security.cert.X509Certificate

/**
 * Checks the BasicConstraints extension on certificates.
 * Leaf must NOT have the CA flag set.
 * Intermediate and Root MUST have the CA flag set and be critical.
 */
internal class BasicConstraintsChecker(private val leafCertificate: X509Certificate) :
    PKIXCertPathChecker() {

    override fun init(forward: Boolean) = Unit
    override fun isForwardCheckingSupported(): Boolean = false
    override fun getSupportedExtensions(): Set<String> = setOf(OID_BASIC_CONSTRAINTS)

    override fun check(cert: Certificate, unresolvedCritExts: MutableCollection<String>?) {
        val x509 = cert as X509Certificate
        val isLeaf = x509 == leafCertificate

        if (isLeaf) {
            if (x509.basicConstraints != -1) {
                throw CertPathValidatorException("Leaf certificate must not have BasicConstraints CA=true")
            }
        } else {
            val isCritical = OID_BASIC_CONSTRAINTS in (x509.criticalExtensionOIDs ?: emptySet())
            if (!isCritical || x509.basicConstraints == -1) {
                throw CertPathValidatorException("CA certificate must have critical BasicConstraints CA=true")
            }
        }
        unresolvedCritExts?.remove(OID_BASIC_CONSTRAINTS)
    }

    companion object {
        private const val OID_BASIC_CONSTRAINTS = "2.5.29.19"
    }
}
