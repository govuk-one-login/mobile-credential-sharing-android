package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.PKIXCertPathChecker
import java.security.cert.X509Certificate

/**
 * Checks the KeyUsage extension on leaf and CA certificates.
 * Leaf must have digitalSignature.
 * CA must have keyCertSign and cRLSign.
 */
internal class KeyUsageChecker(private val leafCertificate: X509Certificate) :
    PKIXCertPathChecker() {

    override fun init(forward: Boolean) = Unit
    override fun isForwardCheckingSupported(): Boolean = false
    override fun getSupportedExtensions(): Set<String> = setOf(OID_KEY_USAGE)

    override fun check(cert: Certificate, unresolvedCritExts: MutableCollection<String>?) {
        val x509 = cert as X509Certificate
        val isLeaf = x509 == leafCertificate

        if (isLeaf) {
            checkLeafKeyUsage(x509)
        } else {
            checkCaKeyUsage(x509)
        }
        unresolvedCritExts?.remove(OID_KEY_USAGE)
    }

    private fun checkLeafKeyUsage(cert: X509Certificate) {
        val isCritical = OID_KEY_USAGE in (cert.criticalExtensionOIDs ?: emptySet())
        val usage = cert.keyUsage
        val isValid = isCritical &&
            usage != null &&
            usage[BIT_DIGITAL_SIGNATURE] &&
            usage.indices.filter { it != BIT_DIGITAL_SIGNATURE }.none { usage[it] }

        if (!isValid) {
            throw CertPathValidatorException("Leaf KeyUsage must be critical and only digitalSignature")
        }
    }

    private fun checkCaKeyUsage(cert: X509Certificate) {
        val usage = cert.keyUsage ?: throw CertPathValidatorException("CA KeyUsage absent")
        val isValid = usage[BIT_KEY_CERT_SIGN] &&
            usage[BIT_CRL_SIGN] &&
            usage.indices.filter { it != BIT_KEY_CERT_SIGN && it != BIT_CRL_SIGN }.none { usage[it] }

        if (!isValid) {
            throw CertPathValidatorException("CA KeyUsage must only contain keyCertSign and cRLSign")
        }
    }

    companion object {
        private const val OID_KEY_USAGE = "2.5.29.15"
        private const val BIT_DIGITAL_SIGNATURE = 0
        private const val BIT_KEY_CERT_SIGN = 5
        private const val BIT_CRL_SIGN = 6
    }
}
