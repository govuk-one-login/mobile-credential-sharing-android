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
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder

/**
 * Generates X509v3 certificates for testing using Bouncy Castle.
 */
object TestCertificateGenerator {
    private var serial = 1L

    @Suppress("LongParameterList")
    fun generate(
        subjectDn: String,
        issuerDn: String,
        subjectKeyPair: KeyPair,
        issuerKeyPair: KeyPair,
        notBefore: Date,
        notAfter: Date,
        isCa: Boolean,
        pathLenConstraint: Int,
        keyUsageBits: IntArray?,
        includeBasicConstraints: Boolean,
        basicConstraintsCritical: Boolean = true,
        keyUsageCritical: Boolean = true,
        akiKeyPair: KeyPair = issuerKeyPair
    ): X509Certificate {
        val signer = JcaContentSignerBuilder("SHA256withECDSA").build(issuerKeyPair.private)

        val builder = JcaX509v3CertificateBuilder(
            X500Name(subjectDnToRfc(issuerDn)),
            BigInteger.valueOf(serial++),
            notBefore,
            notAfter,
            X500Name(subjectDnToRfc(subjectDn)),
            subjectKeyPair.public
        )

        if (includeBasicConstraints) {
            val bc = if (pathLenConstraint >= 0) {
                BasicConstraints(pathLenConstraint)
            } else {
                BasicConstraints(isCa)
            }
            builder.addExtension(Extension.basicConstraints, basicConstraintsCritical, bc)
        }

        if (keyUsageBits != null) {
            var usage = 0
            for (bit in keyUsageBits) {
                usage = usage or keyUsageFlag(bit)
            }
            builder.addExtension(Extension.keyUsage, keyUsageCritical, KeyUsage(usage))
        }

        val skiBytes = org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils()
            .createSubjectKeyIdentifier(subjectKeyPair.public)
        builder.addExtension(Extension.subjectKeyIdentifier, false, skiBytes)

        val akiBytes = org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils()
            .createAuthorityKeyIdentifier(akiKeyPair.public)
        builder.addExtension(Extension.authorityKeyIdentifier, false, akiBytes)

        val holder = builder.build(signer)
        return JcaX509CertificateConverter().getCertificate(holder)
    }

    private fun subjectDnToRfc(dn: String): String = dn

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

    class CertBuilder(
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

        fun withAki(keyPair: KeyPair) = apply {
            akiKeyPair = keyPair
        }

        fun build(): X509Certificate = generate(
            subjectDn = subject,
            issuerDn = issuer,
            subjectKeyPair = keyPair,
            issuerKeyPair = issuerKeyPair,
            notBefore = notBefore,
            notAfter = notAfter,
            isCa = isCa,
            pathLenConstraint = pathLenConstraint,
            keyUsageBits = if (includeKeyUsage) keyUsageBits else null,
            includeBasicConstraints = includeBasicConstraints,
            basicConstraintsCritical = basicConstraintsCritical,
            keyUsageCritical = keyUsageCritical,
            akiKeyPair = akiKeyPair ?: issuerKeyPair
        )

        companion object {
            private const val DIGITAL_SIGNATURE = 0
            private const val KEY_ENCIPHERMENT = 2
            private const val KEY_CERT_SIGN = 5
            private const val CRL_SIGN = 6
        }
    }
}
