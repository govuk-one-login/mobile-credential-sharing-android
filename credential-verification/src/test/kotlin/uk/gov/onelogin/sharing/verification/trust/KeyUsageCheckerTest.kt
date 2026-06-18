package uk.gov.onelogin.sharing.verification.trust

import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator.CertBuilder
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
    fun `leaf without KeyUsage extension throws UNTRUSTED_CERTIFICATE`() {
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
        ).caWithoutKeyUsage().build()

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
    fun `CA intermediate with digitalSignature throws UNTRUSTED_CERTIFICATE`() {
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
        ).caWithLeafKeyUsage().build()

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
    fun `leaf with extra bits set alongside digitalSignature throws UNTRUSTED_CERTIFICATE`() {
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
        ).leafWithExtraBits().build()

        assertVerificationFailure(
            listOf(leaf),
            leafKp,
            root,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }
}
