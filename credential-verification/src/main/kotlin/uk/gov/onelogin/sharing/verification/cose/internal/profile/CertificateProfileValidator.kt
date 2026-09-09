package uk.gov.onelogin.sharing.verification.cose.internal.profile

import dev.zacsweers.metro.Inject
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import uk.gov.onelogin.sharing.verification.cose.CertificateProfileReason
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.CertificateProfileViolation

/**
 * Validates the X.509 certificate profile for a candidate path according to the
 * selected operation purpose ([CertificatePurpose.ISSUER_AUTH] or [CertificatePurpose.READER_AUTH]).
 */
@Suppress("TooManyFunctions")
@Inject
internal class CertificateProfileValidator {

    /**
     * Validates the candidate certificate chain against the role-specific profile.
     *
     * @param chain The complete candidate path (leaf first, excluding root).
     * @param purpose The selected operation purpose.
     * @return The approved candidate leaf certificate.
     * @throws CertificateProfileViolation if any profile rule is violated.
     */
    fun validate(chain: List<X509Certificate>, purpose: CertificatePurpose): X509Certificate {
        if (chain.isEmpty()) {
            throw CertificateProfileViolation(CertificateProfileReason.MISSING_CRITICAL_EXTENSION)
        }

        val leaf = chain.first()

        chain.forEach { cert ->
            validateVersion(cert)
            validateSerialNumber(cert)
            validateSubject(cert)
            validateBasicConstraints(cert, isLeaf = (cert == leaf))
            validateKeyUsage(cert, isLeaf = (cert == leaf))
        }

        validateExtendedKeyUsage(leaf, purpose)
        validateValidityDuration(leaf, purpose)

        return leaf
    }

    private fun validateVersion(cert: X509Certificate) {
        if (cert.version != X509_VERSION_3) {
            throw CertificateProfileViolation(CertificateProfileReason.MISSING_CRITICAL_EXTENSION)
        }
    }

    private fun validateSerialNumber(cert: X509Certificate) {
        val serial = cert.serialNumber
        if (serial == null || serial.signum() <= 0) {
            throw CertificateProfileViolation(CertificateProfileReason.MISSING_CRITICAL_EXTENSION)
        }
    }

    private fun validateSubject(cert: X509Certificate) {
        val dn = cert.subjectX500Principal.getName("RFC2253")
        val country = extractAttribute(dn, ATTR_COUNTRY)
        val commonName = extractAttribute(dn, ATTR_COMMON_NAME)

        if (country != REQUIRED_COUNTRY || commonName == null) {
            throw CertificateProfileViolation(CertificateProfileReason.MISSING_CRITICAL_EXTENSION)
        }
    }

    private fun validateBasicConstraints(cert: X509Certificate, isLeaf: Boolean) {
        if (isLeaf) {
            if (cert.basicConstraints != -1) {
                throw CertificateProfileViolation(CertificateProfileReason.INVALID_KEY_USAGE)
            }
        } else {
            val isCritical = OID_BASIC_CONSTRAINTS in (cert.criticalExtensionOIDs ?: emptySet())
            if (!isCritical || cert.basicConstraints == -1) {
                throw CertificateProfileViolation(CertificateProfileReason.INVALID_KEY_USAGE)
            }
        }
    }

    private fun validateKeyUsage(cert: X509Certificate, isLeaf: Boolean) {
        val usage = cert.keyUsage
        val isValid = usage != null && if (isLeaf) {
            usage[BIT_DIGITAL_SIGNATURE] &&
                usage.indices.filter { it != BIT_DIGITAL_SIGNATURE }.none { usage[it] }
        } else {
            usage[BIT_KEY_CERT_SIGN] &&
                usage[BIT_CRL_SIGN] &&
                usage.indices.filter {
                    it != BIT_KEY_CERT_SIGN && it != BIT_CRL_SIGN
                }.none { usage[it] }
        }

        if (!isValid) {
            throw CertificateProfileViolation(CertificateProfileReason.INVALID_KEY_USAGE)
        }
    }

    private fun validateExtendedKeyUsage(leaf: X509Certificate, purpose: CertificatePurpose) {
        val ekuOids = leaf.extendedKeyUsage?.toSet() ?: emptySet()
        val targetEku = if (purpose == CertificatePurpose.ISSUER_AUTH) {
            OID_ISSUER_AUTH_EKU
        } else {
            OID_READER_AUTH_EKU
        }
        val otherEku = if (purpose == CertificatePurpose.ISSUER_AUTH) {
            OID_READER_AUTH_EKU
        } else {
            OID_ISSUER_AUTH_EKU
        }

        if (otherEku in ekuOids && targetEku !in ekuOids) {
            throw CertificateProfileViolation(CertificateProfileReason.CROSS_PURPOSE_REJECTION)
        }
        if (targetEku !in ekuOids) {
            throw CertificateProfileViolation(
                CertificateProfileReason.INVALID_EXTENDED_KEY_USAGE
            )
        }
    }

    private fun validateValidityDuration(leaf: X509Certificate, purpose: CertificatePurpose) {
        val durationMs = leaf.notAfter.time - leaf.notBefore.time
        val durationDays = TimeUnit.MILLISECONDS.toDays(durationMs)

        val maxAllowedDays = when (purpose) {
            CertificatePurpose.ISSUER_AUTH -> MAX_ISSUER_VALIDITY_DAYS
            CertificatePurpose.READER_AUTH -> MAX_READER_VALIDITY_DAYS
        }

        if (durationDays > maxAllowedDays) {
            throw CertificateProfileViolation(
                CertificateProfileReason.INVALID_VALIDITY_PERIOD
            )
        }
    }

    private fun extractAttribute(dn: String, attr: String): String? {
        val prefix = "$attr="
        val start = dn.indexOf(prefix)
        if (start < 0) return null
        val valueStart = start + prefix.length
        val end = dn.indexOf(',', valueStart).let { if (it < 0) dn.length else it }
        return dn.substring(valueStart, end).trim().ifEmpty { null }
    }

    private companion object {
        const val X509_VERSION_3 = 3
        const val REQUIRED_COUNTRY = "GB"
        const val ATTR_COUNTRY = "C"
        const val ATTR_COMMON_NAME = "CN"

        const val OID_BASIC_CONSTRAINTS = "2.5.29.19"
        const val OID_ISSUER_AUTH_EKU = "1.0.18013.5.1.2"
        const val OID_READER_AUTH_EKU = "1.0.18013.5.1.6"

        const val BIT_DIGITAL_SIGNATURE = 0
        const val BIT_KEY_CERT_SIGN = 5
        const val BIT_CRL_SIGN = 6

        const val MAX_ISSUER_VALIDITY_DAYS = 457L
        const val MAX_READER_VALIDITY_DAYS = 1187L
    }
}
