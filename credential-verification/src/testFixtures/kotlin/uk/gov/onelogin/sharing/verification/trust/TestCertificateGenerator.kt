package uk.gov.onelogin.sharing.verification.trust

import java.math.BigInteger
import java.security.KeyPair
import java.security.cert.X509Certificate
import java.util.Date
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.BasicConstraints
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.KeyUsage
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

/**
 * Generates X509v3 certificates for testing using Bouncy Castle.
 */
class TestCertificateGenerator(
    private val subject: String,
    private val keyPair: KeyPair,
    private val issuerKeyPair: KeyPair,
    private val issuer: String
) {
    private var isCa = false
    private var pathLenConstraint = -1
    private var notBefore: Date = Date(System.currentTimeMillis() - 86400000L)
    private var notAfter: Date = Date(System.currentTimeMillis() + 365L * 86400000L)
    private var keyUsageBits: IntArray? = null
    private var includeKeyUsage = true
    private var includeBasicConstraints = true
    private var basicConstraintsCritical = true
    private var keyUsageCritical = true
    private var akiKeyPair: KeyPair? = null
    private var serialNumber: BigInteger? = null
    private var sigAlgorithm: String = "SHA256withECDSA"
    private var includeAki = true
    private var includeSki = true
    private var extraExtensions: List<ExtensionSpec> = emptyList()

    fun ca(pathLen: Int = -1) = apply {
        isCa = true
        pathLenConstraint = pathLen
        keyUsageBits = intArrayOf(KEY_CERT_SIGN, CRL_SIGN)
    }

    fun caNotCriticalBasicConstraints(pathLen: Int = -1) = apply {
        isCa = true
        pathLenConstraint = pathLen
        basicConstraintsCritical = false
        keyUsageBits = intArrayOf(KEY_CERT_SIGN, CRL_SIGN)
    }

    fun leaf() = apply {
        isCa = false
        includeBasicConstraints = false
        keyUsageBits = intArrayOf(DIGITAL_SIGNATURE)
    }

    fun expired() = apply {
        notBefore = Date(System.currentTimeMillis() - 2 * 365L * 86400000L)
        notAfter = Date(System.currentTimeMillis() - 365L * 86400000L)
    }

    fun notYetValid() = apply {
        notBefore = Date(System.currentTimeMillis() + 365L * 86400000L)
        notAfter = Date(System.currentTimeMillis() + 2 * 365L * 86400000L)
    }

    fun noKeyUsage() = apply {
        includeKeyUsage = false
        includeBasicConstraints = false
    }

    fun caKeyUsage() = apply {
        isCa = false
        includeBasicConstraints = false
        keyUsageBits = intArrayOf(KEY_CERT_SIGN, CRL_SIGN)
    }

    fun caWithLeafKeyUsage() = apply {
        isCa = true
        keyUsageBits = intArrayOf(DIGITAL_SIGNATURE)
    }

    fun leafWithExtraBits() = apply {
        isCa = false
        includeBasicConstraints = false
        keyUsageBits = intArrayOf(DIGITAL_SIGNATURE, KEY_ENCIPHERMENT)
    }

    fun leafWithBasicConstraints() = apply {
        isCa = false
        includeBasicConstraints = true
        keyUsageBits = intArrayOf(DIGITAL_SIGNATURE)
    }

    fun caWithoutBasicConstraints() = apply {
        isCa = true
        includeBasicConstraints = false
        keyUsageBits = intArrayOf(KEY_CERT_SIGN, CRL_SIGN)
    }

    fun caWithCaFlagFalse() = apply {
        isCa = false
        includeBasicConstraints = true
        basicConstraintsCritical = true
        keyUsageBits = intArrayOf(KEY_CERT_SIGN, CRL_SIGN)
    }

    fun leafWithNonCriticalKeyUsage() = apply {
        isCa = false
        includeBasicConstraints = false
        keyUsageBits = intArrayOf(DIGITAL_SIGNATURE)
        keyUsageCritical = false
    }

    fun caWithoutKeyUsage() = apply {
        isCa = true
        includeKeyUsage = false
        keyUsageBits = intArrayOf(KEY_CERT_SIGN, CRL_SIGN)
    }

    fun caKeyCertSignOnly() = apply {
        isCa = true
        keyUsageBits = intArrayOf(KEY_CERT_SIGN)
    }

    fun caWithExtraKeyUsageBits() = apply {
        isCa = true
        keyUsageBits = intArrayOf(KEY_CERT_SIGN, CRL_SIGN, DIGITAL_SIGNATURE)
    }

    fun withAki(keyPair: KeyPair) = apply {
        akiKeyPair = keyPair
    }

    fun withSerial(serial: BigInteger) = apply {
        serialNumber = serial
    }

    fun withSignatureAlgorithm(algorithm: String) = apply {
        sigAlgorithm = algorithm
    }

    fun withoutAki() = apply {
        includeAki = false
    }

    fun withoutSki() = apply {
        includeSki = false
    }

    fun withExtension(oid: String, critical: Boolean, value: ByteArray) = apply {
        extraExtensions = extraExtensions + ExtensionSpec(oid, critical, value)
    }

    fun build(): X509Certificate {
        val signingKey = issuerKeyPair
        val signer = JcaContentSignerBuilder(sigAlgorithm).build(signingKey.private)

        val serial = serialNumber ?: generateValidSerial()

        val builder = JcaX509v3CertificateBuilder(
            X500Name(issuer),
            serial,
            notBefore,
            notAfter,
            X500Name(subject),
            keyPair.public
        )

        if (includeBasicConstraints) {
            val bc = if (pathLenConstraint >= 0) {
                BasicConstraints(pathLenConstraint)
            } else {
                BasicConstraints(isCa)
            }
            builder.addExtension(Extension.basicConstraints, basicConstraintsCritical, bc)
        }

        if (includeKeyUsage && keyUsageBits != null) {
            var usage = 0
            for (bit in keyUsageBits!!) {
                usage = usage or keyUsageFlag(bit)
            }
            builder.addExtension(Extension.keyUsage, keyUsageCritical, KeyUsage(usage))
        }

        val extUtils = JcaX509ExtensionUtils()
        if (includeSki) {
            builder.addExtension(
                Extension.subjectKeyIdentifier,
                false,
                extUtils.createSubjectKeyIdentifier(keyPair.public)
            )
        }
        if (includeAki) {
            builder.addExtension(
                Extension.authorityKeyIdentifier,
                false,
                extUtils.createAuthorityKeyIdentifier((akiKeyPair ?: issuerKeyPair).public)
            )
        }

        for (ext in extraExtensions) {
            builder.addExtension(
                org.bouncycastle.asn1.ASN1ObjectIdentifier(ext.oid),
                ext.critical,
                org.bouncycastle.asn1.DEROctetString(ext.value)
            )
        }

        val holder = builder.build(signer)
        return JcaX509CertificateConverter().getCertificate(holder)
    }

    companion object {
        private var serial = 1L
        private const val DIGITAL_SIGNATURE = 0
        private const val KEY_ENCIPHERMENT = 2
        private const val KEY_CERT_SIGN = 5
        private const val CRL_SIGN = 6
        private const val VALID_SERIAL_OCTETS = 9

        private fun generateValidSerial(): BigInteger = BigInteger(
            1,
            ByteArray(VALID_SERIAL_OCTETS).also { bytes ->
                val value = serial++
                for (i in bytes.indices.reversed()) {
                    bytes[i] = (value shr ((bytes.size - 1 - i) * 8)).toByte()
                }
                if (bytes[0] == 0.toByte()) bytes[0] = 0x01
            }
        )

        private fun keyUsageFlag(bit: Int): Int = when (bit) {
            0 -> KeyUsage.digitalSignature
            1 -> KeyUsage.nonRepudiation
            2 -> KeyUsage.keyEncipherment
            3 -> KeyUsage.dataEncipherment
            4 -> KeyUsage.keyAgreement
            5 -> KeyUsage.keyCertSign
            6 -> KeyUsage.cRLSign
            7 -> KeyUsage.encipherOnly
            8 -> KeyUsage.decipherOnly
            else -> throw IllegalArgumentException("Unknown KeyUsage bit: $bit")
        }
    }
}

data class ExtensionSpec(val oid: String, val critical: Boolean, val value: ByteArray)
