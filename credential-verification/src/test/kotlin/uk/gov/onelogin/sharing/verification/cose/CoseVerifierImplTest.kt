package uk.gov.onelogin.sharing.verification.cose

import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
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

    @Test
    fun `detached reader auth returns verified leaf without copying the payload`() {
        val detachedBytes = CoseVectors.createDetachedVector()
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = detachedBytes,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )

        val result = verifier.verify(request) as CoseVerificationResult.Detached

        assertThat(
            result.leafCertificate.subjectX500Principal.name,
            equalTo("ST=London,C=GB,CN=Reader")
        )
    }

    @Test
    fun `detached reader auth leaf is byte-identical to the x5chain entry`() {
        val detachedBytes = CoseVectors.createDetachedVector()
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = detachedBytes,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )

        val result = verifier.verify(request) as CoseVerificationResult.Detached

        assertThat(
            result.leafCertificate.encoded.toList(),
            equalTo(CoseVectors.readerLeafSignedByRoot.encoded.toList())
        )
    }

    @Test
    fun `mutating the caller payload invalidates the detached signature`() {
        val detachedBytes = CoseVectors.createDetachedVector()
        val mutatedPayload = CoseVectors.detachedReaderAuthPayloadBytes
            .copyOf()
            .also { it[0] = (it[0].toInt() xor 0xFF).toByte() }

        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = detachedBytes,
            detachedPayload = mutatedPayload,
            trustedRoot = trustedRoot
        )

        assertThrows(CoseVerificationFailure.InvalidSignature::class.java) {
            verifier.verify(request)
        }
    }


    @Test
    fun `detached malformed array throws MalformedCoseSign1`() {
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = byteArrayOf(0x83.toByte()),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached attached payload element throws MalformedCoseSign1`() {
        val attachedUsedAsDetached = CoseVectors.createAttachedVector()
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = attachedUsedAsDetached,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached unsupported algorithm throws UnsupportedAlgorithm`() {
        val invalidCose = CoseVectors.createDetachedVector(alg = -35L)
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = invalidCose,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached missing x5chain throws MissingX5Chain`() {
        val invalidCose = CoseVectors.createDetachedVector(includeX5chain = false)
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = invalidCose,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        assertThrows(CoseVerificationFailure.MissingX5Chain::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached untrusted path throws UntrustedCertificate`() {
        val untrustedRoot = TestCertificateGenerator(
            subject = "CN=Untrusted",
            keyPair = CertificateStubs.rootKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Untrusted"
        ).ca().build()
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = untrustedRoot
        )
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached profile violation - issuer EKU throws CertificateProfileViolation`() {
        val invalidCose = CoseVectors.createDetachedVector(eku = "1.0.18013.5.1.2")
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = invalidCose,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        val failure = assertThrows(CoseVerificationFailure.CertificateProfileViolation::class.java) {
            verifier.verify(request)
        }
        assertThat(failure.reason, equalTo(CertificateProfileReason.CROSS_PURPOSE_REJECTION))
    }

    @Test
    fun `detached invalid signature throws InvalidSignature`() {
        val invalidCose = CoseVectors.createDetachedVector(tamperSignature = true)
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = invalidCose,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        assertThrows(CoseVerificationFailure.InvalidSignature::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached reader profile selection is independent of root identity`() {
        val detachedBytes = CoseVectors.createDetachedVector()

        val successRequest = CoseVerificationRequest.Detached(
            coseSign1Bytes = detachedBytes,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        val result = verifier.verify(successRequest)
        assertThat(result is CoseVerificationResult.Detached, equalTo(true))

        val differentRoot = TestCertificateGenerator(
            subject = "CN=OtherRoot,C=GB,ST=London",
            keyPair = CertificateStubs.untrustedRootKeyPair,
            issuerKeyPair = CertificateStubs.untrustedRootKeyPair,
            issuer = "CN=OtherRoot,C=GB,ST=London"
        ).ca().build()
        val failRequest = CoseVerificationRequest.Detached(
            coseSign1Bytes = detachedBytes,
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = differentRoot
        )
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(failRequest)
        }
    }
}
