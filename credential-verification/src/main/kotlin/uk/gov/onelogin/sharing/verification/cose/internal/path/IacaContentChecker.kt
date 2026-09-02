package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.PKIXCertPathChecker
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

internal class IacaContentChecker(
    private val orderedChain: List<X509Certificate>,
    private val trustedRoot: X509Certificate
) : PKIXCertPathChecker() {

    override fun init(forward: Boolean) = Unit
    override fun isForwardCheckingSupported(): Boolean = false
    override fun getSupportedExtensions(): Set<String> = setOf(OID_EKU)

    override fun check(cert: Certificate, unresolvedCritExts: MutableCollection<String>?) {
        val x509 = cert as X509Certificate
        val isLeaf = x509 == orderedChain.first()

        if (isLeaf) {
            verifyExtendedKeyUsage(x509)
            verifyLeafContent(x509)
        }

        verifySubject(x509, isLeaf)
        verifyChainConsistency(x509)

        unresolvedCritExts?.remove(OID_EKU)
    }

    private fun verifyExtendedKeyUsage(leaf: X509Certificate) {
        val isCritical = OID_EKU in (leaf.criticalExtensionOIDs ?: emptySet())
        val ekuOids = leaf.extendedKeyUsage?.toSet()
        val isValid = isCritical &&
            ekuOids != null &&
            OID_MDL_DS in ekuOids &&
            ekuOids.all { it in ALLOWED_EKU_OIDS }

        if (!isValid) {
            throw CertPathValidatorException(
                "ExtendedKeyUsage must be critical, contain $OID_MDL_DS, and no disallowed OIDs"
            )
        }
    }

    private fun verifyLeafContent(leaf: X509Certificate) {
        val durationDays = TimeUnit.MILLISECONDS.toDays(
            leaf.notAfter.time - leaf.notBefore.time
        )
        if (durationDays > MAX_LEAF_VALIDITY_DAYS) {
            throw CertPathValidatorException(
                "Leaf validity exceeds $MAX_LEAF_VALIDITY_DAYS days: $durationDays"
            )
        }

        val rootDn = trustedRoot.subjectX500Principal.getName(DN_FORMAT)
        val rootState = extractAttribute(rootDn, ATTR_STATE) ?: return
        val leafDn = leaf.subjectX500Principal.getName(DN_FORMAT)
        val leafState = extractAttribute(leafDn, ATTR_STATE)
        if (leafState != rootState) {
            throw CertPathValidatorException(
                "Leaf stateOrProvinceName must match root, expected $rootState got $leafState"
            )
        }
    }

    private fun verifySubject(cert: X509Certificate, isLeaf: Boolean) {
        val dn = cert.subjectX500Principal.getName(DN_FORMAT)
        val country = extractAttribute(dn, ATTR_COUNTRY)
        val hasRequiredAttributes = country == REQUIRED_COUNTRY &&
            (!isLeaf || dn.contains("$ATTR_COMMON_NAME="))

        if (!hasRequiredAttributes) {
            throw CertPathValidatorException("Subject must contain C=$REQUIRED_COUNTRY and CN")
        }
    }

    private fun verifyChainConsistency(cert: X509Certificate) {
        val parent = parentOf(cert)
        if (!cert.issuerX500Principal.encoded.contentEquals(parent.subjectX500Principal.encoded)) {
            throw CertPathValidatorException("Issuer field does not match parent Subject")
        }
        if (cert.getExtensionValue(OID_ISSUER_ALT_NAME) == null) {
            throw CertPathValidatorException("IssuerAltName extension absent")
        }
    }

    private fun parentOf(cert: X509Certificate): X509Certificate {
        val index = orderedChain.indexOf(cert)
        return if (index < 0 || index == orderedChain.lastIndex) {
            trustedRoot
        } else {
            orderedChain[index + 1]
        }
    }

    private fun extractAttribute(dn: String, attr: String): String? {
        val prefix = "$attr="
        val start = dn.indexOf(prefix)
        if (start < 0) return null
        val valueStart = start + prefix.length
        val end = dn.indexOf(',', valueStart).let { if (it < 0) dn.length else it }
        return dn.substring(valueStart, end).trim()
    }

    internal companion object {
        const val OID_EKU = "2.5.29.37"
        const val OID_MDL_DS = "1.0.18013.5.1.2"
        const val OID_MDOC_DS = "1.0.23220.4.1.2"
        const val OID_ISSUER_ALT_NAME = "2.5.29.18"
        const val MAX_LEAF_VALIDITY_DAYS = 457L
        private const val REQUIRED_COUNTRY = "GB"
        private const val DN_FORMAT = "RFC2253"
        private const val ATTR_COUNTRY = "C"
        private const val ATTR_COMMON_NAME = "CN"
        private const val ATTR_STATE = "ST"
        val ALLOWED_EKU_OIDS = setOf(OID_MDL_DS, OID_MDOC_DS)
    }
}
