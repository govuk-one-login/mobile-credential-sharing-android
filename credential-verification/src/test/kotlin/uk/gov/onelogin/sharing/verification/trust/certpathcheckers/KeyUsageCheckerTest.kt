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

class KeyUsageCheckerTest : TrustVerificationTest {
    private val logger = SystemLogger()
    override val verifier: TrustVerifier = TrustVerifierImpl(
        CoseSign1Decoder(logger),
        CoseSignatureVerifier(CoseHeaderValidator(logger)),
        logger
    )

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
        assertVerificationFailure(
            listOf(CertificateStubs.leafNoKeyUsage),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `leaf with keyCertSign instead of digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leafWithCaKeyUsage),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `leaf with non-critical KeyUsage throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leafWithNonCriticalKeyUsage),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `CA intermediate without KeyUsage throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leaf, CertificateStubs.caWithoutKeyUsage),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `leaf with extra bits set alongside digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leafWithExtraBits),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }
}
