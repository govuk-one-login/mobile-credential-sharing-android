package uk.gov.onelogin.sharing.verification.trust.certpathcheckers

import java.security.cert.CertPathValidatorException
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.trust.CoseSign1Decoder
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator
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
        val leafKp = generateKeyPair()
        val caKp = generateKeyPair()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).leaf().build()

        val ca = TestCertificateGenerator(
            subject = "CN=CA,C=GB,ST=London",
            keyPair = caKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).caKeyCertSignOnly().build()

        val checker = KeyUsageChecker(leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(ca, mutableSetOf())
        }
    }

    @Test
    fun `check throws when CA has keyCertSign and cRLSign plus extra bits`() {
        val leafKp = generateKeyPair()
        val caKp = generateKeyPair()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).leaf().build()

        val ca = TestCertificateGenerator(
            subject = "CN=CA,C=GB,ST=London",
            keyPair = caKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).caWithExtraKeyUsageBits().build()

        val checker = KeyUsageChecker(leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(ca, mutableSetOf())
        }
    }

    @Test
    fun `check throws when CA has wrong KeyUsage bits`() {
        val leafKp = generateKeyPair()
        val caKp = generateKeyPair()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).leaf().build()

        val ca = TestCertificateGenerator(
            subject = "CN=CA,C=GB,ST=London",
            keyPair = caKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).caWithLeafKeyUsage().build()

        val checker = KeyUsageChecker(leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(ca, mutableSetOf())
        }
    }

    @Test
    fun `leaf without KeyUsage extension throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = generateKeyPair()
        val leafKp = generateKeyPair()

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).noKeyUsage().build()

        assertVerificationFailure(
            listOf(leaf),
            leafKp,
            root,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `leaf with keyCertSign instead of digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = generateKeyPair()
        val leafKp = generateKeyPair()

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).caKeyUsage().build()

        assertVerificationFailure(
            listOf(leaf),
            leafKp,
            root,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `leaf with non-critical KeyUsage throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = generateKeyPair()
        val leafKp = generateKeyPair()

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).leafWithNonCriticalKeyUsage().build()

        assertVerificationFailure(
            listOf(leaf),
            leafKp,
            root,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `CA intermediate without KeyUsage throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = generateKeyPair()
        val intermediateKp = generateKeyPair()
        val leafKp = generateKeyPair()

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        val intermediate = TestCertificateGenerator(
            subject = "CN=Intermediate,C=GB,ST=London",
            keyPair = intermediateKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).caWithoutKeyUsage().build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = intermediateKp,
            issuer = "CN=Intermediate,C=GB,ST=London"
        ).leaf().build()

        assertVerificationFailure(
            listOf(leaf, intermediate),
            leafKp,
            root,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `leaf with extra bits set alongside digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = generateKeyPair()
        val leafKp = generateKeyPair()

        val root = TestCertificateGenerator(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        val leaf = TestCertificateGenerator(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).leafWithExtraBits().build()

        assertVerificationFailure(
            listOf(leaf),
            leafKp,
            root,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }
}
