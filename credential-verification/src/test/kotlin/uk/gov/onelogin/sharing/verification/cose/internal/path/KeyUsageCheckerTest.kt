package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.CertPathValidatorException
import org.junit.Assert.assertThrows
import org.junit.Test

class KeyUsageCheckerTest {

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
    fun `leaf without KeyUsage extension throws CertPathValidatorException`() {
        val checker = KeyUsageChecker(CertificateStubs.leafNoKeyUsage)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.leafNoKeyUsage, mutableSetOf())
        }
    }

    @Test
    fun `leaf with keyCertSign instead of digitalSignature throws CertPathValidatorException`() {
        val checker = KeyUsageChecker(CertificateStubs.leafWithCaKeyUsage)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.leafWithCaKeyUsage, mutableSetOf())
        }
    }

    @Test
    fun `CA intermediate without KeyUsage throws CertPathValidatorException`() {
        val checker = KeyUsageChecker(CertificateStubs.leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.caWithoutKeyUsage, mutableSetOf())
        }
    }

    @Test
    fun `leaf with extra bits set alongside digitalSignature throws CertPathValidatorException`() {
        val checker = KeyUsageChecker(CertificateStubs.leafWithExtraBits)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.leafWithExtraBits, mutableSetOf())
        }
    }
}
