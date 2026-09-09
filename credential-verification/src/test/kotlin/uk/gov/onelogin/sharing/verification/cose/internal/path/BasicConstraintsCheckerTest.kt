package uk.gov.onelogin.sharing.verification.cose.internal.path

import java.security.cert.CertPathValidatorException
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.CoseVerificationFailure.UntrustedCertificate

class BasicConstraintsCheckerTest {
    private val validator: CertificateChainValidator = CertificateChainValidatorImpl()

    @Test
    fun `check passes for valid leaf without BasicConstraints extension`() {
        val checker = BasicConstraintsChecker(CertificateStubs.leaf)
        checker.init(false)
        val unresolvedCritExts = mutableSetOf("2.5.29.19")

        checker.check(CertificateStubs.leaf, unresolvedCritExts)

        assertThat(unresolvedCritExts.contains("2.5.29.19"), equalTo(false))
    }

    @Test
    fun `check passes for valid leaf with BasicConstraints extension when cA is false`() {
        val checker = BasicConstraintsChecker(CertificateStubs.leafWithBasicConstraints)
        checker.init(false)
        val unresolvedCritExts = mutableSetOf("2.5.29.19")

        checker.check(CertificateStubs.leafWithBasicConstraints, unresolvedCritExts)

        assertThat(unresolvedCritExts.contains("2.5.29.19"), equalTo(false))
    }

    @Test
    fun `check throws when leaf has BasicConstraints with cA flag true`() {
        val checker = BasicConstraintsChecker(CertificateStubs.rootCa)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.rootCa, mutableSetOf()) // rootCa has cA=true
        }
    }

    @Test
    fun `check passes for valid CA with critical BasicConstraints and cA flag true`() {
        val checker = BasicConstraintsChecker(CertificateStubs.leaf)
        checker.init(false)
        val unresolvedCritExts = mutableSetOf("2.5.29.19")

        checker.check(CertificateStubs.intermediateCa, unresolvedCritExts)

        assertThat(unresolvedCritExts.contains("2.5.29.19"), equalTo(false))
    }

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
    fun `check throws when CA has non-critical BasicConstraints extension`() {
        val checker = BasicConstraintsChecker(CertificateStubs.leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(CertificateStubs.caNotCriticalBasicConstraints, mutableSetOf())
        }
    }

    @Test
    fun `valid certificate chain passes verification`() {
        val result = runCatching {
            validator.verify(
                listOf(CertificateStubs.leaf, CertificateStubs.intermediateCa),
                CertificateStubs.rootCa
            )
        }

        assertThat(result.isSuccess, equalTo(true))
    }

    @Test
    fun `intermediate with non-critical BasicConstraints throws UNTRUSTED_CERTIFICATE`() {
        assertThrows(UntrustedCertificate::class.java) {
            validator.verify(
                listOf(CertificateStubs.leaf, CertificateStubs.caNotCriticalBasicConstraints),
                CertificateStubs.rootCa
            )
        }
    }

    @Test
    fun `intermediate with cA flag false throws UNTRUSTED_CERTIFICATE`() {
        assertThrows(UntrustedCertificate::class.java) {
            validator.verify(
                listOf(CertificateStubs.leaf, CertificateStubs.caWithCaFlagFalse),
                CertificateStubs.rootCa
            )
        }
    }
}
