package uk.gov.onelogin.sharing.verification.trust.certpathcheckers

import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.PKIXCertPathChecker
import java.security.cert.X509Certificate

private const val KEY_USAGE_OID = "2.5.29.15"

private const val DIGITAL_SIGNATURE = 0
private const val KEY_CERT_SIGN = 5
private const val CRL_SIGN = 6

internal class KeyUsageChecker(private val leafCertificate: X509Certificate) :
    PKIXCertPathChecker() {

    override fun init(forward: Boolean) = Unit

    override fun isForwardCheckingSupported(): Boolean = false

    override fun getSupportedExtensions(): Set<String> = setOf(KEY_USAGE_OID)

    override fun check(cert: Certificate, unresolvedCritExts: MutableCollection<String>?) {
        val x509 = cert as X509Certificate
        val keyUsage = x509.keyUsage
            ?: throw CertPathValidatorException("KeyUsage extension absent")

        if (!x509.criticalExtensionOIDs.contains(KEY_USAGE_OID)) {
            throw CertPathValidatorException("KeyUsage extension not marked critical")
        }

        if (x509 == leafCertificate) {
            validateLeafKeyUsage(keyUsage)
        } else {
            validateCaKeyUsage(keyUsage)
        }

        unresolvedCritExts?.remove(KEY_USAGE_OID)
    }

    private fun validateLeafKeyUsage(keyUsage: BooleanArray) {
        val valid = keyUsage[DIGITAL_SIGNATURE] &&
            keyUsage.indices.none { i -> i != DIGITAL_SIGNATURE && keyUsage[i] }

        if (!valid) {
            throw CertPathValidatorException("Leaf KeyUsage must have only digitalSignature")
        }
    }

    private fun validateCaKeyUsage(keyUsage: BooleanArray) {
        val valid = keyUsage[KEY_CERT_SIGN] &&
            keyUsage[CRL_SIGN] &&
            keyUsage.indices.none { i -> i != KEY_CERT_SIGN && i != CRL_SIGN && keyUsage[i] }

        if (!valid) {
            throw CertPathValidatorException("CA KeyUsage must have only keyCertSign and cRLSign")
        }
    }
}
