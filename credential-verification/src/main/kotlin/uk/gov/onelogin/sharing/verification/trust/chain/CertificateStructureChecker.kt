package uk.gov.onelogin.sharing.verification.trust.chain

import java.security.MessageDigest
import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.security.cert.PKIXCertPathChecker
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import uk.gov.onelogin.sharing.verification.trust.extractSubjectPublicKeyBits
import uk.gov.onelogin.sharing.verification.trust.subjectKeyIdentifierHex
import uk.gov.onelogin.sharing.verification.trust.toHexString

internal class CertificateStructureChecker(
    private val orderedChain: List<X509Certificate>,
    private val trustedRoot: X509Certificate
) : PKIXCertPathChecker() {

    override fun init(forward: Boolean) = Unit
    override fun isForwardCheckingSupported(): Boolean = false
    override fun getSupportedExtensions(): Set<String>? = null

    override fun check(cert: Certificate, unresolvedCritExts: MutableCollection<String>?) {
        val x509 = cert as X509Certificate
        verifyCriticalExtensions(x509)
        verifyForbiddenExtensions(x509)
        verifyDuplicateExtensions(x509)
        verifySerialNumber(x509)
        verifySubjectKeyIdentifier(x509)
        verifyAuthorityKeyIdentifier(x509)
        verifyAlgorithmStrength(x509)
    }

    private fun verifyCriticalExtensions(cert: X509Certificate) {
        val critical = cert.criticalExtensionOIDs ?: return
        for (oid in critical) {
            if (oid !in ALLOWED_CRITICAL_OIDS) {
                throw CertPathValidatorException(
                    "Disallowed critical extension: $oid"
                )
            }
        }
    }

    private fun verifyForbiddenExtensions(cert: X509Certificate) {
        val critical = cert.criticalExtensionOIDs.orEmpty()
        val nonCritical = cert.nonCriticalExtensionOIDs.orEmpty()
        FORBIDDEN_OIDS.firstOrNull { it in critical || it in nonCritical }?.let {
            throw CertPathValidatorException("Forbidden extension present: $it")
        }
    }

    private fun verifyDuplicateExtensions(cert: X509Certificate) {
        val critical = cert.criticalExtensionOIDs ?: emptySet()
        val nonCritical = cert.nonCriticalExtensionOIDs ?: emptySet()
        if (critical.any { it in nonCritical }) {
            throw CertPathValidatorException("Duplicate extension OID detected")
        }
    }

    private fun verifySerialNumber(cert: X509Certificate) {
        val serial = cert.serialNumber
        if (serial.signum() <= 0) {
            throw CertPathValidatorException("Serial number must be positive non-zero")
        }
        val bytes = serial.toByteArray()
        val octets = if (bytes.isNotEmpty() && bytes[0] == 0.toByte()) {
            bytes.size - 1
        } else {
            bytes.size
        }
        if (octets !in MIN_SERIAL_OCTETS..MAX_SERIAL_OCTETS) {
            throw CertPathValidatorException(
                "Serial number must be $MIN_SERIAL_OCTETS-$MAX_SERIAL_OCTETS octets, got $octets"
            )
        }
    }

    private fun verifySubjectKeyIdentifier(cert: X509Certificate) {
        requireNonCriticalExtension(cert, OID_SKI, "SubjectKeyIdentifier")
        val skiHex = cert.subjectKeyIdentifierHex()
            ?: throw CertPathValidatorException("SubjectKeyIdentifier absent")
        val bits = extractSubjectPublicKeyBits(cert.publicKey.encoded)
            ?: throw CertPathValidatorException("Cannot extract subject public key bits")
        val expectedHex = MessageDigest.getInstance("SHA-1").digest(bits).toHexString()
        if (skiHex != expectedHex) {
            throw CertPathValidatorException("SubjectKeyIdentifier does not match public key hash")
        }
    }

    private fun verifyAuthorityKeyIdentifier(cert: X509Certificate) {
        requireNonCriticalExtension(cert, OID_AKI, "AuthorityKeyIdentifier")
    }

    private fun requireNonCriticalExtension(
        cert: X509Certificate,
        oid: String,
        name: String
    ) {
        if (oid in (cert.criticalExtensionOIDs ?: emptySet())) {
            throw CertPathValidatorException("$name must not be critical")
        }
        if (cert.getExtensionValue(oid) == null) {
            throw CertPathValidatorException("$name absent")
        }
    }

    private fun verifyAlgorithmStrength(cert: X509Certificate) {
        val issuer = issuerOf(cert)
        val issuerKey = issuer.publicKey as? ECPublicKey
            ?: throw CertPathValidatorException("Issuer public key is not EC")
        val certStrength = SIG_ALGORITHM_STRENGTH[cert.sigAlgOID]
            ?: throw CertPathValidatorException("Disallowed signing algorithm: ${cert.sigAlgOID}")
        val curveSize = issuerKey.params.order.bitLength()
        val minStrength = when {
            curveSize <= CURVE_256 -> STRENGTH_SHA256
            curveSize <= CURVE_384 -> STRENGTH_SHA384
            else -> STRENGTH_SHA512
        }
        if (certStrength < minStrength) {
            throw CertPathValidatorException(
                "Algorithm strength insufficient for issuer's ${curveSize}-bit curve"
            )
        }
    }

    private fun issuerOf(cert: X509Certificate): X509Certificate {
        val index = orderedChain.indexOf(cert)
        return if (index < 0 || index == orderedChain.lastIndex) {
            trustedRoot
        } else {
            orderedChain[index + 1]
        }
    }

    internal companion object {
        const val MIN_SERIAL_OCTETS = 9
        const val MAX_SERIAL_OCTETS = 20
        const val CURVE_256 = 256
        const val CURVE_384 = 384
        const val STRENGTH_SHA256 = 1
        const val STRENGTH_SHA384 = 2
        const val STRENGTH_SHA512 = 3

        const val OID_SKI = "2.5.29.14"
        const val OID_AKI = "2.5.29.35"

        val ALLOWED_CRITICAL_OIDS = setOf(
            "2.5.29.19", // BasicConstraints
            "2.5.29.15", // KeyUsage
            "2.5.29.37"  // ExtendedKeyUsage
        )

        val FORBIDDEN_OIDS = setOf(
            "2.5.29.33", // PolicyMappings
            "2.5.29.30", // NameConstraints
            "2.5.29.36", // PolicyConstraints
            "2.5.29.54", // InhibitAnyPolicy
            "2.5.29.46"  // FreshestCRL
        )

        val SIG_ALGORITHM_STRENGTH = mapOf(
            "1.2.840.10045.4.3.2" to STRENGTH_SHA256,
            "1.2.840.10045.4.3.3" to STRENGTH_SHA384,
            "1.2.840.10045.4.3.4" to STRENGTH_SHA512
        )
    }
}
