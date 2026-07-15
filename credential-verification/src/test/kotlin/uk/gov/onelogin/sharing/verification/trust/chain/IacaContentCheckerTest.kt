package uk.gov.onelogin.sharing.verification.trust.chain

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Date
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator

@RunWith(TestParameterInjector::class)
class IacaContentCheckerTest {
    private val validator = CertificateChainValidatorImpl(SystemLogger())

    private val rootDn = "CN=Root,C=GB,ST=London"
    private val leafDn = "CN=Leaf,C=GB,ST=London"

    private fun validLeaf() = TestCertificateGenerator(
        subject = leafDn,
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = rootDn
    ).leaf()

    // AC1: Happy path
    @Test
    fun `valid chain with all content checks passes`() {
        val leaf = validLeaf().build()
        validator.verify(listOf(leaf), CertificateStubs.rootCa)
    }

    @Test
    fun `valid chain with mdlDS and mdocDS EKU passes`() {
        val leaf = validLeaf()
            .withEku(listOf(IacaContentChecker.OID_MDL_DS, IacaContentChecker.OID_MDOC_DS))
            .build()
        validator.verify(listOf(leaf), CertificateStubs.rootCa)
    }

    // AC2: Invalid ExtendedKeyUsage
    @Test
    fun `leaf without EKU throws UNTRUSTED_CERTIFICATE`() {
        val leaf = validLeaf().withoutEku().build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `leaf with non-critical EKU throws UNTRUSTED_CERTIFICATE`() {
        val leaf = validLeaf()
            .withEku(listOf(IacaContentChecker.OID_MDL_DS), critical = false)
            .build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `leaf with EKU missing mdlDS throws UNTRUSTED_CERTIFICATE`() {
        val leaf = validLeaf()
            .withEku(listOf(IacaContentChecker.OID_MDOC_DS))
            .build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `leaf with disallowed EKU OID throws UNTRUSTED_CERTIFICATE`() {
        val leaf = validLeaf()
            .withEku(listOf(IacaContentChecker.OID_MDL_DS, "1.3.6.1.5.5.7.3.1"))
            .build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // AC3: Missing required Subject attributes
    @Test
    fun `leaf without countryName throws UNTRUSTED_CERTIFICATE`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `leaf without commonName throws UNTRUSTED_CERTIFICATE`() {
        val leaf = TestCertificateGenerator(
            subject = "C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // AC4: Country code not GB
    @Test
    fun `leaf with country not GB throws UNTRUSTED_CERTIFICATE`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=US,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // AC5: Inconsistent stateOrProvinceName
    @Test
    fun `leaf without ST when root has ST throws UNTRUSTED_CERTIFICATE`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `leaf with different ST than root throws UNTRUSTED_CERTIFICATE`() {
        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=Manchester",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = rootDn
        ).leaf().build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // AC6: Subject/Issuer mismatch
    @Test
    fun `leaf with issuer not matching parent subject throws UNTRUSTED_CERTIFICATE`() {
        val leaf = TestCertificateGenerator(
            subject = leafDn,
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=WrongIssuer,C=GB,ST=London"
        ).leaf().build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // AC7: Leaf validity exceeds 457 days
    @Test
    fun `leaf with validity exceeding 457 days throws UNTRUSTED_CERTIFICATE`() {
        val now = System.currentTimeMillis()
        val leaf = validLeaf()
            .withValidity(
                notBefore = Date(now - 86400000L),
                notAfter = Date(now + (IacaContentChecker.MAX_LEAF_VALIDITY_DAYS + 1) * 86400000L)
            )
            .build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // AC8: Missing IssuerAltName
    @Test
    fun `leaf without IssuerAltName throws UNTRUSTED_CERTIFICATE`() {
        val leaf = validLeaf().withoutIssuerAltName().build()
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(listOf(leaf), CertificateStubs.rootCa)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }
}
