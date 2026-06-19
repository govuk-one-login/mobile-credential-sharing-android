package uk.gov.onelogin.sharing.verification.trust

import org.junit.Assert.assertNotNull
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.trust.cose.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.trust.cose.CoseSignatureVerifier

class CertificateChainValidationTest : TrustVerificationTest {
    private val logger = SystemLogger()
    private val decoder = CoseSign1Decoder(logger)
    override val verifier: TrustVerifier = TrustVerifierImpl(
        decoder,
        CoseSignatureVerifier(CoseHeaderValidator(logger)),
        logger
    )

    // Happy path
    @Test
    fun `valid chain with root-intermediate-leaf validates successfully`() {
        val coseSign1 = buildCoseSign1WithChain(
            listOf(CertificateStubs.leaf, CertificateStubs.intermediateCa),
            CertificateStubs.leafKeyPair
        )
        val result = verifier.verifyCOSESign1(coseSign1, CertificateStubs.rootCa)

        assertNotNull(result.msoPayload)
        assertNotNull(result.certificateValidityPeriod)
    }

    @Test
    fun `valid chain with root-leaf validates successfully`() {
        val coseSign1 = buildCoseSign1WithChain(
            listOf(CertificateStubs.leafSignedByRoot),
            CertificateStubs.leafKeyPair
        )
        val result = verifier.verifyCOSESign1(coseSign1, CertificateStubs.rootCa)

        assertNotNull(result.msoPayload)
    }

    // Untrusted root
    @Test
    fun `chain not anchored to provided root throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leafSignedByUntrusted),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    // Invalid date/time validity
    @Test
    fun `expired leaf certificate throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.expiredLeaf),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `not-yet-valid leaf certificate throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.notYetValidLeaf),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    // Invalid signature
    @Test
    fun `intermediate signed by wrong key throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leaf, CertificateStubs.intermediateSignedByWrongKey),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    // AKI/SKI mismatch
    @Test
    fun `AKI-SKI mismatch throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leafWithWrongAki),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    // Invalid BasicConstraints
    @Test
    fun `intermediate without CA flag throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(CertificateStubs.leaf, CertificateStubs.intermediateAsLeaf),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }

    @Test
    fun `pathLenConstraint violation throws UNTRUSTED_CERTIFICATE`() {
        assertVerificationFailure(
            listOf(
                CertificateStubs.leafSignedByInter2,
                CertificateStubs.inter2Ca,
                CertificateStubs.intermediateWithPathLen0
            ),
            CertificateStubs.leafKeyPair,
            CertificateStubs.rootCa,
            VerificationError.UNTRUSTED_CERTIFICATE
        )
    }
}
