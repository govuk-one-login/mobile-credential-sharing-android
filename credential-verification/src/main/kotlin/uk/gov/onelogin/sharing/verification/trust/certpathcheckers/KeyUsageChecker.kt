package uk.gov.onelogin.sharing.verification.trust.certpathcheckers

import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.PKIXCertPathChecker
import java.security.cert.X509Certificate

private const val KEY_USAGE_OID = "2.5.29.15"

// KeyUsage bit positions
private const val DIGITAL_SIGNATURE = 0
private const val KEY_CERT_SIGN = 5
private const val CRL_SIGN = 6

internal class KeyUsageChecker(
    private val leafCertificate: X509Certificate
) : PKIXCertPathChecker() {

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
        if (!keyUsage[DIGITAL_SIGNATURE]) {
            throw CertPathValidatorException("Leaf missing digitalSignature bit")
        }
        for (i in keyUsage.indices) {
            if (i != DIGITAL_SIGNATURE && keyUsage[i]) {
                throw CertPathValidatorException("Leaf has unexpected KeyUsage bit at position $i")
            }
        }
    }

    private fun validateCaKeyUsage(keyUsage: BooleanArray) {
        if (!keyUsage[KEY_CERT_SIGN]) {
            throw CertPathValidatorException("CA missing keyCertSign bit")
        }
        if (!keyUsage[CRL_SIGN]) {
            throw CertPathValidatorException("CA missing cRLSign bit")
        }
        for (i in keyUsage.indices) {
            if (i != KEY_CERT_SIGN && i != CRL_SIGN && keyUsage[i]) {
                throw CertPathValidatorException("CA has unexpected KeyUsage bit at position $i")
            }
        }
    }
}
