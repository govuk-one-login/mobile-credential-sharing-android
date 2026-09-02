package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.CertPathValidatorException
import java.security.cert.X509Certificate
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure

class KeyUsageCheckerTest {
    private val validator: CertificateChainValidator = CertificateChainValidatorImpl()

    @Test
    fun `check throws when CA has keyCertSign but missing cRLSign`() {
        val checker = KeyUsageChecker(CertificateStubs.leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.caKeyCertSignOnly, mutableSetOf())
        }
    }

    @Test
    fun `check throws when CA has keyCertSign and cRLSign plus extra bits`() {
        val checker = KeyUsageChecker(CertificateStubs.leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.caWithExtraKeyUsageBits, mutableSetOf())
        }
    }

    @Test
    fun `check throws when CA has wrong KeyUsage bits`() {
        val checker = KeyUsageChecker(CertificateStubs.leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.caWithLeafKeyUsage, mutableSetOf())
        }
    }

    @Test
    fun `leaf without KeyUsage extension throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leafNoKeyUsage),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `leaf with keyCertSign instead of digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leafWithCaKeyUsage),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `leaf with non-critical KeyUsage throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leafWithNonCriticalKeyUsage),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `CA intermediate without KeyUsage throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leaf, CertificateStubs.caWithoutKeyUsage),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `leaf with extra bits set alongside digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leafWithExtraBits),
            CertificateStubs.rootCa
        )
    }

    private fun assertValidationFailure(chain: List<X509Certificate>, root: X509Certificate) {
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(chain, root)
        }
    }
}
