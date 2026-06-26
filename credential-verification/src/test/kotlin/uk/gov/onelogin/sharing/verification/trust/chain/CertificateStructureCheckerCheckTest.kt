package uk.gov.onelogin.sharing.verification.trust.chain

import io.mockk.every
import io.mockk.mockk
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.cert.CertPathValidatorException
import java.security.cert.X509Certificate
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator
import uk.gov.onelogin.sharing.verification.trust.chain.CertificateStructureChecker.Companion.FORBIDDEN_OIDS

class CertificateStructureCheckerCheckTest {

    private val checker = CertificateStructureChecker(
        orderedChain = listOf(CertificateStubs.leafSignedByRoot),
        trustedRoot = CertificateStubs.rootCa
    )

    @Test
    fun `check passes for valid leaf certificate`() {
        checker.init(false)
        checker.check(CertificateStubs.leafSignedByRoot, mutableSetOf())
    }

    @Test
    fun `check throws for disallowed critical extension`() {
        val cert = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withExtension("1.3.6.1.4.1.99999.1", true, byteArrayOf(0x01)).build()

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(cert, mutableSetOf())
        }
    }

    @Test
    fun `check throws for forbidden extension`() {
        val oid = FORBIDDEN_OIDS.first()
        val cert = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withExtension(oid, false, byteArrayOf(0x30, 0x00)).build()

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(cert, mutableSetOf())
        }
    }

    @Test
    fun `check throws for duplicate extension OID`() {
        val duplicateOid = "2.5.29.15"
        val mockCert = mockk<X509Certificate>(relaxed = true)
        every { mockCert.criticalExtensionOIDs } returns setOf(duplicateOid)
        every { mockCert.nonCriticalExtensionOIDs } returns setOf(duplicateOid)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(mockCert, mutableSetOf())
        }
    }

    @Test
    fun `check throws for missing AuthorityKeyIdentifier`() {
        val cert = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withoutAki().build()

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(cert, mutableSetOf())
        }
    }

    @Test
    fun `check throws for missing SubjectKeyIdentifier`() {
        val cert = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withoutSki().build()

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(cert, mutableSetOf())
        }
    }

    @Test
    fun `check throws for disallowed signing algorithm`() {
        val rsaKeyPair = KeyPairGenerator.getInstance("RSA").apply {
            initialize(2048)
        }.generateKeyPair()

        val cert = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = rsaKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSignatureAlgorithm("SHA256withRSA").build()

        val checkerWithRsaRoot = CertificateStructureChecker(
            orderedChain = listOf(cert),
            trustedRoot = CertificateStubs.rootCa
        )

        assertThrows(CertPathValidatorException::class.java) {
            checkerWithRsaRoot.check(cert, mutableSetOf())
        }
    }

    @Test
    fun `check throws for invalid serial number`() {
        val cert = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withSerial(BigInteger.ZERO).build()

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(cert, mutableSetOf())
        }
    }
}
