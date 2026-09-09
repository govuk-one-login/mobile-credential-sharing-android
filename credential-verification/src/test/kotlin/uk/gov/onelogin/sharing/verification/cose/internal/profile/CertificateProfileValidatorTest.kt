package uk.gov.onelogin.sharing.verification.cose.internal.profile

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.Date
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verification.cose.CertificateProfileReason
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.CertificateProfileViolation
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator

@RunWith(TestParameterInjector::class)
class CertificateProfileValidatorTest {

    private val validator = CertificateProfileValidator()

    private val validIssuerLeaf = CertificateStubs.leaf
    private val intermediate = CertificateStubs.intermediateCa

    @Test
    fun `valid IssuerAuth certificate path is approved and returns leaf`() {
        val result = validator.validate(
            listOf(validIssuerLeaf, intermediate),
            CertificatePurpose.ISSUER_AUTH
        )

        assertThat(result, equalTo(validIssuerLeaf))
    }

    @Test
    fun `valid ReaderAuth certificate path is approved and returns leaf`() {
        val now = System.currentTimeMillis()
        val readerLeaf = TestCertificateGenerator(
            subject = "CN=Reader Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf()
            .withEkuOids(listOf("1.0.18013.5.1.6"))
            .withValidity(Date(now), Date(now + 1000L * 86400000L))
            .build()

        val result = validator.validate(
            listOf(readerLeaf),
            CertificatePurpose.READER_AUTH
        )

        assertThat(result, equalTo(readerLeaf))
    }

    @Test
    fun `leaf with BasicConstraints cA true fails with INVALID_KEY_USAGE`() {
        val violation = assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(
                listOf(CertificateStubs.rootCa),
                CertificatePurpose.ISSUER_AUTH
            )
        }

        assertThat(violation.reason, equalTo(CertificateProfileReason.INVALID_KEY_USAGE))
    }

    @Test
    fun `intermediate with non-critical BasicConstraints fails with INVALID_KEY_USAGE`() {
        val violation = assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(
                listOf(validIssuerLeaf, CertificateStubs.caNotCriticalBasicConstraints),
                CertificatePurpose.ISSUER_AUTH
            )
        }

        assertThat(violation.reason, equalTo(CertificateProfileReason.INVALID_KEY_USAGE))
    }

    @Test
    fun `intermediate with extra KeyUsage bits fails with INVALID_KEY_USAGE`() {
        val violation = assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(
                listOf(validIssuerLeaf, CertificateStubs.caWithExtraKeyUsageBits),
                CertificatePurpose.ISSUER_AUTH
            )
        }

        assertThat(violation.reason, equalTo(CertificateProfileReason.INVALID_KEY_USAGE))
    }

    @Test
    fun `leaf with extra KeyUsage bits fails with INVALID_KEY_USAGE`() {
        val violation = assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(
                listOf(CertificateStubs.leafWithExtraBits),
                CertificatePurpose.ISSUER_AUTH
            )
        }

        assertThat(violation.reason, equalTo(CertificateProfileReason.INVALID_KEY_USAGE))
    }

    @Test
    fun `certificate with non-GB country fails with profile violation`() {
        val nonGbCert = TestCertificateGenerator(
            subject = "CN=Leaf,C=US,ST=California",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().build()

        assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(listOf(nonGbCert), CertificatePurpose.ISSUER_AUTH)
        }
    }

    @Test
    fun `certificate without CommonName fails with profile violation`() {
        val noCnCert = TestCertificateGenerator(
            subject = "C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().build()

        assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(listOf(noCnCert), CertificatePurpose.ISSUER_AUTH)
        }
    }

    @Test
    fun `IssuerAuth leaf with duration exceeding 457 days fails with INVALID_VALIDITY_PERIOD`() {
        val now = System.currentTimeMillis()
        val longValidityLeaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf()
            .withValidity(Date(now), Date(now + 458L * 86400000L))
            .build()

        val violation = assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(listOf(longValidityLeaf), CertificatePurpose.ISSUER_AUTH)
        }

        assertThat(violation.reason, equalTo(CertificateProfileReason.INVALID_VALIDITY_PERIOD))
    }

    @Test
    fun `ReaderAuth leaf with duration exceeding 1187 days fails with INVALID_VALIDITY_PERIOD`() {
        val now = System.currentTimeMillis()
        val longValidityReaderLeaf = TestCertificateGenerator(
            subject = "CN=Reader Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf()
            .withEkuOids(listOf("1.0.18013.5.1.6"))
            .withValidity(Date(now), Date(now + 1188L * 86400000L))
            .build()

        val violation = assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(listOf(longValidityReaderLeaf), CertificatePurpose.READER_AUTH)
        }

        assertThat(violation.reason, equalTo(CertificateProfileReason.INVALID_VALIDITY_PERIOD))
    }

    @Test
    fun `IssuerAuth with Reader EKU fails with CROSS_PURPOSE_REJECTION`() {
        val readerLeaf = TestCertificateGenerator(
            subject = "CN=Reader Leaf,C=GB,ST=London",
            keyPair = CertificateStubs.leafKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf()
            .withEkuOids(listOf("1.0.18013.5.1.6"))
            .build()

        val violation = assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(listOf(readerLeaf), CertificatePurpose.ISSUER_AUTH)
        }

        assertThat(violation.reason, equalTo(CertificateProfileReason.CROSS_PURPOSE_REJECTION))
    }

    @Test
    fun `ReaderAuth with Issuer EKU fails with CROSS_PURPOSE_REJECTION`() {
        val violation = assertThrows(CertificateProfileViolation::class.java) {
            validator.validate(listOf(validIssuerLeaf), CertificatePurpose.READER_AUTH)
        }

        assertThat(violation.reason, equalTo(CertificateProfileReason.CROSS_PURPOSE_REJECTION))
    }
}
