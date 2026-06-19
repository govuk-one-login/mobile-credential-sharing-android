package uk.gov.onelogin.sharing.verification.trust.chain

import java.security.cert.X509Certificate
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResultMatchers
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs

class CertificateChainValidationTest {
    private val validator: CertificateChainValidator = CertificateChainValidatorImpl(SystemLogger())

    // Happy path
    @Test
    fun `valid chain with root-intermediate-leaf validates successfully`() {
        validator.verify(
            listOf(CertificateStubs.leaf, CertificateStubs.intermediateCa),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `valid chain with root-leaf validates successfully`() {
        validator.verify(
            listOf(CertificateStubs.leafSignedByRoot),
            CertificateStubs.rootCa
        )
    }

    // Untrusted root
    @Test
    fun `chain not anchored to provided root throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leafSignedByUntrusted),
            CertificateStubs.rootCa
        )
    }

    // Invalid date/time validity
    @Test
    fun `expired leaf certificate throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.expiredLeaf),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `not-yet-valid leaf certificate throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.notYetValidLeaf),
            CertificateStubs.rootCa
        )
    }

    // Invalid signature
    @Test
    fun `intermediate signed by wrong key throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leaf, CertificateStubs.intermediateSignedByWrongKey),
            CertificateStubs.rootCa
        )
    }

    // AKI/SKI mismatch
    @Test
    fun `AKI-SKI mismatch throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leafWithWrongAki),
            CertificateStubs.rootCa
        )
    }

    // Invalid BasicConstraints
    @Test
    fun `intermediate without CA flag throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(CertificateStubs.leaf, CertificateStubs.intermediateAsLeaf),
            CertificateStubs.rootCa
        )
    }

    @Test
    fun `pathLenConstraint violation throws UNTRUSTED_CERTIFICATE`() {
        assertValidationFailure(
            listOf(
                CertificateStubs.leafSignedByInter2,
                CertificateStubs.inter2Ca,
                CertificateStubs.intermediateWithPathLen0
            ),
            CertificateStubs.rootCa
        )
    }

    private fun assertValidationFailure(chain: List<X509Certificate>, root: X509Certificate) {
        val exception = assertThrows(VerificationResult.Failure::class.java) {
            validator.verify(chain, root)
        }
        MatcherAssert.assertThat(
            exception,
            VerificationResultMatchers
                .hasError(VerificationError.UNTRUSTED_CERTIFICATE)
        )
    }
}
