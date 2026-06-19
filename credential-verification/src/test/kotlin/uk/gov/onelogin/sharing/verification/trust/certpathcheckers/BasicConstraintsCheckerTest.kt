package uk.gov.onelogin.sharing.verification.trust.certpathcheckers

import java.security.cert.CertPathValidatorException
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.CoseSign1Decoder
import uk.gov.onelogin.sharing.verification.trust.TrustVerificationTest
import uk.gov.onelogin.sharing.verification.trust.TrustVerifier
import uk.gov.onelogin.sharing.verification.trust.TrustVerifierImpl
import uk.gov.onelogin.sharing.verification.trust.cose.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.trust.cose.CoseSignatureVerifier

class BasicConstraintsCheckerTest : TrustVerificationTest {
    private val logger = SystemLogger()
    override val verifier: TrustVerifier = TrustVerifierImpl(
        CoseSign1Decoder(logger),
        CoseSignatureVerifier(CoseHeaderValidator(logger)),
        logger
    )

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
        assertVerificationFailure(
            listOf(CertificateStubs.leaf, CertificateStubs.caNotCriticalBasicConstraints),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `leaf with BasicConstraints extension present throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leafWithBasicConstraints),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }
}
