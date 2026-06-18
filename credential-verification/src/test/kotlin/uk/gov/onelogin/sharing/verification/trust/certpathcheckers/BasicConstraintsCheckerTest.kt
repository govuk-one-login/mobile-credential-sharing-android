package uk.gov.onelogin.sharing.verification.trust.certpathcheckers

import java.security.cert.CertPathValidatorException
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.trust.CoseSign1Decoder
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator.CertBuilder
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
        val leafKp = generateKeyPair()
        val caKp = generateKeyPair()

        val leaf = CertBuilder(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).leaf().build()

        val ca = CertBuilder(
            subject = "CN=CA,C=GB,ST=London",
            keyPair = caKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).caWithoutBasicConstraints().build()

        val checker = BasicConstraintsChecker(leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(ca, mutableSetOf())
        }
    }

    @Test
    fun `check throws when CA has BasicConstraints but cA flag is false`() {
        val leafKp = generateKeyPair()
        val caKp = generateKeyPair()

        val leaf = CertBuilder(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).leaf().build()

        val ca = CertBuilder(
            subject = "CN=CA,C=GB,ST=London",
            keyPair = caKp,
            issuerKeyPair = caKp,
            issuer = "CN=CA,C=GB,ST=London"
        ).caWithCaFlagFalse().build()

        val checker = BasicConstraintsChecker(leaf)
        checker.init(false)

        assertThrows(CertPathValidatorException::class.java) {
            checker.check(ca, mutableSetOf())
        }
    }

    @Test
    fun `intermediate with non-critical BasicConstraints throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = generateKeyPair()
        val intermediateKp = generateKeyPair()
        val leafKp = generateKeyPair()

        val root = CertBuilder(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        val intermediate = CertBuilder(
            subject = "CN=Intermediate,C=GB,ST=London",
            keyPair = intermediateKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).caNotCriticalBasicConstraints().build()

        val leaf = CertBuilder(
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
    fun `leaf with BasicConstraints extension present throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = generateKeyPair()
        val leafKp = generateKeyPair()

        val root = CertBuilder(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        val leaf = CertBuilder(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).leafWithBasicConstraints().build()

        assertVerificationFailure(
            listOf(leaf),
            leafKp,
            root,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }
}
