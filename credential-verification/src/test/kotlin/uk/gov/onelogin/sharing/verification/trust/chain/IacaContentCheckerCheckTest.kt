package uk.gov.onelogin.sharing.verification.trust.chain

import io.mockk.mockk
import java.security.cert.CertPathValidatorException
import java.security.cert.Certificate
import java.util.Date
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator
import uk.gov.onelogin.sharing.verification.trust.chain.IacaContentChecker.Companion.MAX_LEAF_VALIDITY_DAYS
import uk.gov.onelogin.sharing.verification.trust.chain.IacaContentChecker.Companion.OID_MDL_DS
import uk.gov.onelogin.sharing.verification.trust.chain.IacaContentChecker.Companion.OID_MDOC_DS

class IacaContentCheckerCheckTest {

    private val rootDn = "CN=Root,C=GB,ST=London"
    private val leafDn = "CN=Leaf,C=GB,ST=London"

    private fun validLeaf() = TestCertificateGenerator(
        subject = leafDn,
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = rootDn
    ).leaf()

    private fun checkerFor(leaf: java.security.cert.X509Certificate) = IacaContentChecker(
        orderedChain = listOf(leaf),
        trustedRoot = CertificateStubs.rootCa
    )

    @Test
    fun `check passes for leaf with mdlDS EKU`() {
        val leaf = validLeaf().build()
        checkerFor(leaf).check(leaf, mutableSetOf())
    }

    @Test
    fun `check passes for leaf with mdlDS and mdocDS EKU`() {
        val leaf = validLeaf().withEku(listOf(OID_MDL_DS, OID_MDOC_DS)).build()
        checkerFor(leaf).check(leaf, mutableSetOf())
    }

    @Test
    fun `check throws for leaf without EKU`() {
        val leaf = validLeaf().withoutEku().build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for leaf with non-critical EKU`() {
        val leaf = validLeaf().withEku(listOf(OID_MDL_DS), critical = false).build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for leaf with EKU missing mdlDS`() {
        val leaf = validLeaf().withEku(listOf(OID_MDOC_DS)).build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for leaf with disallowed EKU OID`() {
        val leaf = validLeaf().withEku(listOf(OID_MDL_DS, "1.3.6.1.5.5.7.3.1")).build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for leaf without countryName`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for leaf without commonName`() {
        val leaf = TestCertificateGenerator(
            subject = "C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for cert with country not GB`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=US,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for issuer not matching parent subject`() {
        val leaf = TestCertificateGenerator(
            subject = leafDn,
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=WrongIssuer,C=GB,ST=London"
        ).leaf().build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for leaf missing ST when root has ST`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws for leaf with different ST than root`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=Manchester",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check passes when root has no ST`() {
        val rootNoState = TestCertificateGenerator(
            subject = "CN=Root,C=GB",
            keyPair = CertificateStubs.rootKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB"
        ).ca().build()
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB"
        ).leaf().build()
        val checker = IacaContentChecker(listOf(leaf), rootNoState)
        checker.check(leaf, mutableSetOf())
    }

    @Test
    fun `check throws for leaf validity exceeding max days`() {
        val now = System.currentTimeMillis()
        val leaf = validLeaf()
            .withValidity(
                notBefore = Date(now - 86400000L),
                notAfter = Date(now + (MAX_LEAF_VALIDITY_DAYS + 1) * 86400000L)
            )
            .build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check passes for leaf validity at exactly max days`() {
        val now = System.currentTimeMillis()
        val leaf = validLeaf()
            .withValidity(
                notBefore = Date(now),
                notAfter = Date(now + MAX_LEAF_VALIDITY_DAYS * 86400000L)
            )
            .build()
        checkerFor(leaf).check(leaf, mutableSetOf())
    }

    @Test
    fun `check throws for missing IssuerAltName`() {
        val leaf = validLeaf().withoutIssuerAltName().build()
        assertThrows(CertPathValidatorException::class.java) {
            checkerFor(leaf).check(leaf, mutableSetOf())
        }
    }

    @Test
    fun `check throws ClassCastException for non-X509 certificate`() {
        val nonX509 = mockk<Certificate>()
        val checker = checkerFor(validLeaf().build())
        assertThrows(ClassCastException::class.java) {
            checker.check(nonX509, mutableSetOf())
        }
    }
}
