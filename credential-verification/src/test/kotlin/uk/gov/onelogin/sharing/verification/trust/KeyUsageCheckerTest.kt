package uk.gov.onelogin.sharing.verification.trust

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator.CertBuilder
import uk.gov.onelogin.sharing.verification.trust.cose.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.trust.cose.CoseSignatureVerifier

class KeyUsageCheckerTest {
    private val logger = SystemLogger()
    private val verifier = TrustVerifierImpl(
        CoseSign1Decoder(logger),
        CoseSignatureVerifier(CoseHeaderValidator(logger)),
        logger
    )

    @Test
    fun `leaf without KeyUsage extension throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

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

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf), leafKp)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `leaf with keyCertSign instead of digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

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

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf), leafKp)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `CA intermediate with digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val intermediateKp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

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

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf, intermediate), leafKp)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `leaf with extra bits set alongside digitalSignature throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

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

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf), leafKp)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }
}
