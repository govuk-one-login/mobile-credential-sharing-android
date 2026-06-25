package uk.gov.onelogin.sharing.verification.trust

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import java.math.BigInteger
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.verification.trust.chain.CertificateStructureChecker.Companion.ALLOWED_CRITICAL_OIDS
import uk.gov.onelogin.sharing.verification.trust.chain.CertificateStructureChecker.Companion.FORBIDDEN_OIDS
import uk.gov.onelogin.sharing.verification.trust.chain.CertificateStructureChecker.Companion.MAX_SERIAL_OCTETS
import uk.gov.onelogin.sharing.verification.trust.chain.CertificateStructureChecker.Companion.MIN_SERIAL_OCTETS

data class StructureCase(
    val description: String,
    val chain: List<X509Certificate>,
    val root: X509Certificate
) {
    override fun toString(): String = description
}

class DisallowedCriticalExtProvider : TestParameterValuesProvider() {
    /**
     * OIDs not in [ALLOWED_CRITICAL_OIDS] that should be rejected when marked critical.
     */
    private val disallowedOids = listOf(
        "1.3.6.1.4.1.99999.1",
        "1.3.6.1.4.1.99999.2"
    ).also { oids ->
        require(oids.none { it in ALLOWED_CRITICAL_OIDS })
    }

    override fun provideValues(context: Context?): List<StructureCase> = disallowedOids.map { oid ->
        StructureCase(
            description = "critical extension $oid not in ALLOWED_CRITICAL_OIDS",
            chain = listOf(
                TestCertificateGenerator(
                    subject = LEAF_DN,
                    keyPair = CertificateStubs.leafKeyPair,
                    issuerKeyPair = CertificateStubs.rootKeyPair,
                    issuer = ROOT_DN
                ).leaf().withExtension(oid, true, byteArrayOf(0x01)).build()
            ),
            root = CertificateStubs.rootCa
        )
    }
}

class ForbiddenExtensionProvider : TestParameterValuesProvider() {
    private val minimalSequence = byteArrayOf(0x30, 0x00)

    override fun provideValues(context: Context?): List<StructureCase> = FORBIDDEN_OIDS.map { oid ->
        StructureCase(
            description = "Forbidden OID $oid present",
            chain = listOf(
                TestCertificateGenerator(
                    subject = LEAF_DN,
                    keyPair = CertificateStubs.leafKeyPair,
                    issuerKeyPair = CertificateStubs.rootKeyPair,
                    issuer = ROOT_DN
                ).leaf().withExtension(oid, false, minimalSequence).build()
            ),
            root = CertificateStubs.rootCa
        )
    }
}

class InvalidSerialProvider : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<StructureCase> = listOf(
        StructureCase(
            description = "serial too short (${MIN_SERIAL_OCTETS - 1} octets)",
            chain = listOf(
                TestCertificateGenerator(
                    subject = LEAF_DN,
                    keyPair = CertificateStubs.leafKeyPair,
                    issuerKeyPair = CertificateStubs.rootKeyPair,
                    issuer = ROOT_DN
                ).leaf().withSerial(
                    BigInteger(
                        1,
                        ByteArray(MIN_SERIAL_OCTETS - 1) {
                            0x7F
                        }
                    )
                ).build()
            ),
            root = CertificateStubs.rootCa
        ),
        StructureCase(
            description = "serial too long (${MAX_SERIAL_OCTETS + 1} octets)",
            chain = listOf(
                TestCertificateGenerator(
                    subject = LEAF_DN,
                    keyPair = CertificateStubs.leafKeyPair,
                    issuerKeyPair = CertificateStubs.rootKeyPair,
                    issuer = ROOT_DN
                ).leaf().withSerial(
                    BigInteger(
                        1,
                        ByteArray(MAX_SERIAL_OCTETS + 1) {
                            0x7F
                        }
                    )
                ).build()
            ),
            root = CertificateStubs.rootCa
        ),
        StructureCase(
            description = "serial is zero",
            chain = listOf(
                TestCertificateGenerator(
                    subject = LEAF_DN,
                    keyPair = CertificateStubs.leafKeyPair,
                    issuerKeyPair = CertificateStubs.rootKeyPair,
                    issuer = ROOT_DN
                ).leaf().withSerial(BigInteger.ZERO).build()
            ),
            root = CertificateStubs.rootCa
        ),
        StructureCase(
            description = "serial is negative",
            chain = listOf(
                TestCertificateGenerator(
                    subject = LEAF_DN,
                    keyPair = CertificateStubs.leafKeyPair,
                    issuerKeyPair = CertificateStubs.rootKeyPair,
                    issuer = ROOT_DN
                ).leaf().withSerial(BigInteger("-1")).build()
            ),
            root = CertificateStubs.rootCa
        )
    )
}

private const val ROOT_DN = "CN=Root,C=GB,ST=London"
private const val LEAF_DN = "CN=Leaf,C=GB,ST=London"
