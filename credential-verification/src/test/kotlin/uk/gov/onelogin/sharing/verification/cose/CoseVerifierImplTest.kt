package uk.gov.onelogin.sharing.verification.cose

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CertificateHeaderValidator
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseSign1Decoder
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateChainValidatorImpl
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator
import uk.gov.onelogin.sharing.verification.cose.internal.profile.CertificateProfileValidator
import uk.gov.onelogin.sharing.verification.cose.internal.signature.CoseSignatureVerifier

@RunWith(TestParameterInjector::class)
class CoseVerifierImplTest {

    private val decoder = CoseSign1Decoder()
    private val headerValidator = CertificateHeaderValidator()
    private val pathValidator = CertificateChainValidatorImpl()
    private val profileValidator = CertificateProfileValidator()
    private val signatureVerifier = CoseSignatureVerifier(CoseHeaderValidator())

    private val verifier = CoseVerifierImpl(
        decoder,
        headerValidator,
        pathValidator,
        profileValidator,
        signatureVerifier
    )

    private val trustedRoot = CertificateStubs.rootCa

    @Test
    fun `attached issuer auth returns verified leaf and exact payload`() {
        val attachedVectorBytes = CoseVectors.attachedMsoBytes
        val request = CoseVerificationRequest.Attached(attachedVectorBytes, trustedRoot)

        val result = verifier.verify(request) as CoseVerificationResult.Attached

        assertThat(result.payload, equalTo(CoseVectors.attachedMsoPayloadBytes))
        assertThat(
            result.leafCertificate.subjectX500Principal.name,
            equalTo("ST=London,C=GB,CN=Leaf")
        )
    }

    @Test
    fun `malformed array throws MalformedCoseSign1`() {
        val request = CoseVerificationRequest.Attached(byteArrayOf(0x83.toByte()), trustedRoot)
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `unsupported algorithm throws UnsupportedAlgorithm`() {
        val invalidCose = CoseVectors.createAttachedVector(alg = -35L)
        val request = CoseVerificationRequest.Attached(invalidCose, trustedRoot)
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `missing x5chain throws MissingX5Chain`() {
        val invalidCose = CoseVectors.createAttachedVector(includeX5chain = false)
        val request = CoseVerificationRequest.Attached(invalidCose, trustedRoot)
        assertThrows(CoseVerificationFailure.MissingX5Chain::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `untrusted path throws UntrustedCertificate`() {
        val untrustedRoot = TestCertificateGenerator(
            subject = "CN=Untrusted",
            keyPair = CertificateStubs.rootKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Untrusted"
        ).ca().build()
        val request = CoseVerificationRequest.Attached(CoseVectors.attachedMsoBytes, untrustedRoot)
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `profile violation throws CertificateProfileViolation`() {
        val invalidCose = CoseVectors.createAttachedVector(eku = "1.0.18013.5.1.6")
        val request = CoseVerificationRequest.Attached(invalidCose, trustedRoot)
        val failure =
            assertThrows(CoseVerificationFailure.CertificateProfileViolation::class.java) {
                verifier.verify(request)
            }
        assertThat(failure.reason, equalTo(CertificateProfileReason.CROSS_PURPOSE_REJECTION))
    }

    @Test
    fun `invalid signature throws InvalidSignature`() {
        val invalidCose = CoseVectors.createAttachedVector(tamperSignature = true)
        val request = CoseVerificationRequest.Attached(invalidCose, trustedRoot)
        assertThrows(CoseVerificationFailure.InvalidSignature::class.java) {
            verifier.verify(request)
        }
    }
}
