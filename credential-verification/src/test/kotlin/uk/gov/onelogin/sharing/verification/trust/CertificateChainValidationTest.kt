package uk.gov.onelogin.sharing.verification.trust

import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers.hasError
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator.CertBuilder
import uk.gov.onelogin.sharing.verification.trust.cose.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.trust.cose.CoseSignatureVerifier

/**
 * Tests for certificate chain structural validation.
 *
 * Covers acceptance criteria 1-6:
 * - AC1: Happy path (valid chain)
 * - AC2: Untrusted root
 * - AC3: Invalid date/time validity
 * - AC4: Invalid signature
 * - AC5: AKI/SKI mismatch
 * - AC6: Invalid BasicConstraints
 */
class CertificateChainValidationTest {
    private val logger = SystemLogger()
    private val decoder = CoseSign1Decoder(logger)
    private val verifier = TrustVerifierImpl(
        decoder,
        CoseSignatureVerifier(CoseHeaderValidator(logger))
    )

    // Happy path
    @Test
    fun `valid chain with root-intermediate-leaf validates successfully`() {
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
        ).ca().build()

        val leaf = CertBuilder(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = intermediateKp,
            issuer = "CN=Intermediate,C=GB,ST=London"
        ).leaf().build()

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf, intermediate), leafKp)
        val result = verifier.verifyCOSESign1(coseSign1, root)

        assertNotNull(result.msoPayload)
        assertNotNull(result.certificateValidityPeriod)
    }

    @Test
    fun `valid chain with root-leaf validates successfully`() {
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
        ).leaf().build()

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf), leafKp)
        val result = verifier.verifyCOSESign1(coseSign1, root)

        assertNotNull(result.msoPayload)
    }

    // Untrusted root
    @Test
    fun `chain not anchored to provided root throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val untrustedRootKp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

        val trustedRoot = CertBuilder(
            subject = "CN=Trusted,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Trusted,C=GB,ST=London"
        ).ca().build()

        val leaf = CertBuilder(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = untrustedRootKp,
            issuer = "CN=Untrusted,C=GB,ST=London"
        ).leaf().build()

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf), leafKp)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, trustedRoot)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // Invalid date/time validity ---
    @Test
    fun `expired leaf certificate throws UNTRUSTED_CERTIFICATE`() {
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
        ).leaf().expired().build()

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf), leafKp)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    @Test
    fun `not-yet-valid leaf certificate throws UNTRUSTED_CERTIFICATE`() {
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
        ).leaf().notYetValid().build()

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf), leafKp)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // Invalid signature
    @Test
    fun `intermediate signed by wrong key throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val wrongKp = CertTestHelpers.generateKeyPair()
        val intermediateKp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

        val root = CertBuilder(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        // Intermediate claims Root as issuer but is signed by wrongKp
        val intermediate = CertBuilder(
            subject = "CN=Intermediate,C=GB,ST=London",
            keyPair = intermediateKp,
            issuerKeyPair = wrongKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

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

    // AKI/SKI mismatch
    @Test
    fun `AKI-SKI mismatch throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val unrelatedKp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

        val root = CertBuilder(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        // Leaf is validly signed by root, but AKI points to an unrelated key
        val leaf = CertBuilder(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().withAki(unrelatedKp).build()

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(listOf(leaf), leafKp)

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }

    // Invalid BasicConstraints
    @Test
    fun `intermediate without CA flag throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val intermediateKp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

        val root = CertBuilder(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        // Intermediate without CA=true (end-entity profile)
        val intermediate = CertBuilder(
            subject = "CN=Intermediate,C=GB,ST=London",
            keyPair = intermediateKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).leaf().build()

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
    fun `pathLenConstraint violation throws UNTRUSTED_CERTIFICATE`() {
        val rootKp = CertTestHelpers.generateKeyPair()
        val inter1Kp = CertTestHelpers.generateKeyPair()
        val inter2Kp = CertTestHelpers.generateKeyPair()
        val leafKp = CertTestHelpers.generateKeyPair()

        val root = CertBuilder(
            subject = "CN=Root,C=GB,ST=London",
            keyPair = rootKp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca().build()

        // inter1 has pathLen=0 meaning no further CAs can exist below it
        val inter1 = CertBuilder(
            subject = "CN=Inter1,C=GB,ST=London",
            keyPair = inter1Kp,
            issuerKeyPair = rootKp,
            issuer = "CN=Root,C=GB,ST=London"
        ).ca(pathLen = 0).build()

        // inter2 violates inter1's pathLen constraint
        val inter2 = CertBuilder(
            subject = "CN=Inter2,C=GB,ST=London",
            keyPair = inter2Kp,
            issuerKeyPair = inter1Kp,
            issuer = "CN=Inter1,C=GB,ST=London"
        ).ca().build()

        val leaf = CertBuilder(
            subject = "CN=Leaf,C=GB,ST=London",
            keyPair = leafKp,
            issuerKeyPair = inter2Kp,
            issuer = "CN=Inter2,C=GB,ST=London"
        ).leaf().build()

        val coseSign1 = CertTestHelpers.buildCoseSign1WithChain(
            listOf(leaf, inter2, inter1),
            leafKp
        )

        val exception = assertThrows(VerificationResult.Failure::class.java) {
            verifier.verifyCOSESign1(coseSign1, root)
        }
        assertThat(exception, hasError(VerificationError.UNTRUSTED_CERTIFICATE))
    }
}
