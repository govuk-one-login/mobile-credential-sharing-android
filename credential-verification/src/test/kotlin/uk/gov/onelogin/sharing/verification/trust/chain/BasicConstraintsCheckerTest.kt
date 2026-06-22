package uk.gov.onelogin.sharing.verification.trust.chain

import java.security.cert.CertPathValidatorException
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs

class BasicConstraintsCheckerTest {
    private val validator: CertificateChainValidator = CertificateChainValidatorImpl(SystemLogger())

    @Test
    fun `check throws when CA missing BasicConstraints extension`() {
        val checker = BasicConstraintsChecker(CertificateStubs.leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.caWithoutBasicConstraints, mutableSetOf())
        }
    }

    @Test
    fun `check throws when CA has BasicConstraints but cA flag is false`() {
        val checker = BasicConstraintsChecker(CertificateStubs.leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.caWithCaFlagFalse, mutableSetOf())
        }
    }

    @Test
    fun `intermediate with non-critical BasicConstraints throws UNTRUSTED_CERTIFICATE`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(
                listOf(CertificateStubs.leaf, CertificateStubs.caNotCriticalBasicConstraints),
                CertificateStubs.rootCa
            )
        }
        assertThat(
            exception,
            hasError(VerificationError.UNTRUSTED_CERTIFICATE)
        )
    }

    @Test
    fun `leaf with BasicConstraints extension present throws UNTRUSTED_CERTIFICATE`() {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(
                listOf(CertificateStubs.leafWithBasicConstraints),
                CertificateStubs.rootCa
            )
        }
        assertThat(
            exception,
            hasError(VerificationError.UNTRUSTED_CERTIFICATE)
        )
    }
}
