package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.CertPathValidatorException
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure

class BasicConstraintsCheckerTest {
    private val validator: CertificateChainValidator = CertificateChainValidatorImpl()

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
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(
                listOf(CertificateStubs.leaf, CertificateStubs.caNotCriticalBasicConstraints),
                CertificateStubs.rootCa
            )
        }
    }

    @Test
    fun `leaf with BasicConstraints extension present throws UNTRUSTED_CERTIFICATE`() {
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            validator.verify(
                listOf(CertificateStubs.leafWithBasicConstraints),
                CertificateStubs.rootCa
            )
        }
    }
}
