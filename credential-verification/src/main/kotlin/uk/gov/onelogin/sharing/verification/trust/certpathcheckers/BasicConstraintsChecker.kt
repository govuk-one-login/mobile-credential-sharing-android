package uk.gov.onelogin.sharing.verification.trust.certpathcheckers

import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.PKIXCertPathChecker
import java.security.cert.X509Certificate

private const val BASIC_CONSTRAINTS_OID = "2.5.29.19"

internal class BasicConstraintsChecker(private val leafCertificate: X509Certificate) :
    PKIXCertPathChecker() {

    override fun init(forward: Boolean) = Unit

    override fun isForwardCheckingSupported(): Boolean = false

    override fun getSupportedExtensions(): Set<String> = setOf(BASIC_CONSTRAINTS_OID)

    override fun check(cert: Certificate, unresolvedCritExts: MutableCollection<String>?) {
        val x509 = cert as X509Certificate

        if (x509 == leafCertificate) {
            if (x509.getExtensionValue(BASIC_CONSTRAINTS_OID) != null) {
                throw CertPathValidatorException("Leaf must not have BasicConstraints extension")
            }
        } else {
            if (x509.getExtensionValue(BASIC_CONSTRAINTS_OID) == null) {
                throw CertPathValidatorException("CA missing BasicConstraints extension")
            }
            if (!x509.criticalExtensionOIDs.contains(BASIC_CONSTRAINTS_OID)) {
                throw CertPathValidatorException("BasicConstraints not marked critical on CA")
            }
            if (x509.basicConstraints < 0) {
                throw CertPathValidatorException("BasicConstraints cA flag not set")
            }
        }

        unresolvedCritExts?.remove(BASIC_CONSTRAINTS_OID)
    }
}
