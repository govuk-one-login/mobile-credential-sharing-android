package uk.gov.onelogin.sharing.verification.cose

import io.mockk.spyk
import io.mockk.verify
import java.security.KeyPairGenerator
import java.security.interfaces.ECPublicKey
import java.security.spec.ECGenParameterSpec
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CertificateHeaderValidator
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseHeaderValidator
import uk.gov.onelogin.sharing.verification.cose.internal.decode.CoseSign1Decoder
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateChainValidatorImpl
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator
import uk.gov.onelogin.sharing.verification.cose.internal.profile.CertificateProfileValidator
import uk.gov.onelogin.sharing.verification.cose.internal.signature.CoseSignatureVerifier

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
        val request = CoseVerificationRequest.Attached(CoseVectors.attachedMsoBytes, trustedRoot)

        val result = verifier.verify(request) as CoseVerificationResult.Attached

        assertThat(result.payload, equalTo(CoseVectors.attachedMsoPayloadBytes))
        assertThat(
            result.leafCertificate.subjectX500Principal.name,
            equalTo("ST=London,C=GB,CN=Leaf")
        )
    }

    @Test
    fun `attached malformed array throws MalformedCoseSign1`() {
        val request = CoseVerificationRequest.Attached(byteArrayOf(0x83.toByte()), trustedRoot)
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `attached unsupported algorithm throws UnsupportedAlgorithm`() {
        val request = CoseVerificationRequest.Attached(
            CoseVectors.createAttachedVector(alg = -35L),
            trustedRoot
        )
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `attached missing x5chain throws MissingX5Chain`() {
        val request = CoseVerificationRequest.Attached(
            CoseVectors.createAttachedVector(includeX5chain = false),
            trustedRoot
        )
        assertThrows(CoseVerificationFailure.MissingX5Chain::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `attached untrusted path throws UntrustedCertificate`() {
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
    fun `attached profile violation throws CertificateProfileViolation`() {
        val request = CoseVerificationRequest.Attached(
            CoseVectors.createAttachedVector(eku = "1.0.18013.5.1.6"),
            trustedRoot
        )
        val failure =
            assertThrows(CoseVerificationFailure.CertificateProfileViolation::class.java) {
                verifier.verify(request)
            }
        assertThat(failure.reason, equalTo(CertificateProfileReason.CROSS_PURPOSE_REJECTION))
    }

    @Test
    fun `attached invalid signature throws InvalidSignature`() {
        val request = CoseVerificationRequest.Attached(
            CoseVectors.createAttachedVector(tamperSignature = true),
            trustedRoot
        )
        assertThrows(CoseVerificationFailure.InvalidSignature::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached reader auth returns verified leaf without copying the payload`() {
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(),
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
    fun `detached leaf is byte-identical to the x5chain entry`() {
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(),
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
    fun `detached mutating the payload invalidates the signature`() {
        val mutated = CoseVectors.detachedReaderAuthPayloadBytes.copyOf()
            .also { it[0] = (it[0].toInt() xor 0xFF).toByte() }
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(),
            detachedPayload = mutated,
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
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createAttachedVector(),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached unsupported algorithm throws UnsupportedAlgorithm`() {
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(alg = -35L),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached missing x5chain throws MissingX5Chain`() {
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(includeX5chain = false),
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
    fun `detached issuer EKU throws CertificateProfileViolation`() {
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(eku = "1.0.18013.5.1.2"),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        val failure =
            assertThrows(CoseVerificationFailure.CertificateProfileViolation::class.java) {
                verifier.verify(request)
            }
        assertThat(failure.reason, equalTo(CertificateProfileReason.CROSS_PURPOSE_REJECTION))
    }

    @Test
    fun `detached invalid signature throws InvalidSignature`() {
        val request = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(tamperSignature = true),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoot = trustedRoot
        )
        assertThrows(CoseVerificationFailure.InvalidSignature::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `detached reader profile selection is independent of root identity`() {
        val result = verifier.verify(
            CoseVerificationRequest.Detached(
                coseSign1Bytes = CoseVectors.createDetachedVector(),
                detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
                trustedRoot = trustedRoot
            )
        )
        assertThat(result is CoseVerificationResult.Detached, equalTo(true))

        val differentRoot = TestCertificateGenerator(
            subject = "CN=OtherRoot,C=GB,ST=London",
            keyPair = CertificateStubs.untrustedRootKeyPair,
            issuerKeyPair = CertificateStubs.untrustedRootKeyPair,
            issuer = "CN=OtherRoot,C=GB,ST=London"
        ).ca().build()
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(
                CoseVerificationRequest.Detached(
                    coseSign1Bytes = CoseVectors.createDetachedVector(),
                    detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
                    trustedRoot = differentRoot
                )
            )
        }
    }

    @Test
    fun `key-based valid direct-key detached signature returns KeyBased`() {
        val request = CoseVerificationRequest.KeyBased(
            coseSign1Bytes = CoseVectors.createKeyBasedVector(),
            detachedPayload = CoseVectors.keyBasedPayloadBytes,
            publicKey = CoseVectors.devicePublicKey
        )

        val result = verifier.verify(request)

        assertThat(result, equalTo(CoseVerificationResult.KeyBased))
    }

    @Test
    fun `key-based mutating the payload invalidates the signature`() {
        val mutated = CoseVectors.keyBasedPayloadBytes.copyOf()
            .also { it[0] = (it[0].toInt() xor 0xFF).toByte() }
        val request = CoseVerificationRequest.KeyBased(
            coseSign1Bytes = CoseVectors.createKeyBasedVector(),
            detachedPayload = mutated,
            publicKey = CoseVectors.devicePublicKey
        )
        assertThrows(CoseVerificationFailure.InvalidSignature::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `key-based absent cert headers succeeds and no cert stage runs`() {
        val spyHeaderValidator = spyk(headerValidator)
        val spyPathValidator = spyk(pathValidator)
        val spyProfileValidator = spyk(profileValidator)
        val isolatedVerifier = CoseVerifierImpl(
            decoder,
            spyHeaderValidator,
            spyPathValidator,
            spyProfileValidator,
            signatureVerifier
        )

        val result = isolatedVerifier.verify(
            CoseVerificationRequest.KeyBased(
                coseSign1Bytes = CoseVectors.createKeyBasedVector(),
                detachedPayload = CoseVectors.keyBasedPayloadBytes,
                publicKey = CoseVectors.devicePublicKey
            )
        )

        assertThat(result, equalTo(CoseVerificationResult.KeyBased))
        verify(exactly = 0) { spyHeaderValidator.validate(any()) }
        verify(exactly = 0) { spyPathValidator.verify(any(), any()) }
        verify(exactly = 0) { spyProfileValidator.validate(any(), any()) }
    }

    @Test
    fun `key-based protected x5bag and x5t, unprotected x5chain succeeds no cert processing`() {
        val spyHeaderValidator = spyk(headerValidator)
        val spyPathValidator = spyk(pathValidator)
        val spyProfileValidator = spyk(profileValidator)
        val isolatedVerifier = CoseVerifierImpl(
            decoder,
            spyHeaderValidator,
            spyPathValidator,
            spyProfileValidator,
            signatureVerifier
        )

        val result = isolatedVerifier.verify(
            CoseVerificationRequest.KeyBased(
                coseSign1Bytes = CoseVectors.createKeyBasedVector(includeCertHeaders = true),
                detachedPayload = CoseVectors.keyBasedPayloadBytes,
                publicKey = CoseVectors.devicePublicKey
            )
        )

        assertThat(result, equalTo(CoseVerificationResult.KeyBased))
        verify(exactly = 0) { spyHeaderValidator.validate(any()) }
        verify(exactly = 0) { spyPathValidator.verify(any(), any()) }
        verify(exactly = 0) { spyProfileValidator.validate(any(), any()) }
    }

    @Test
    fun `key-based unprotected x5bag and x5t plus protected x5chain succeeds no cert processing`() {
        val spyHeaderValidator = spyk(headerValidator)
        val spyPathValidator = spyk(pathValidator)
        val spyProfileValidator = spyk(profileValidator)
        val isolatedVerifier = CoseVerifierImpl(
            decoder,
            spyHeaderValidator,
            spyPathValidator,
            spyProfileValidator,
            signatureVerifier
        )

        val result = isolatedVerifier.verify(
            CoseVerificationRequest.KeyBased(
                coseSign1Bytes = CoseVectors.createKeyBasedVector(swapCertHeaders = true),
                detachedPayload = CoseVectors.keyBasedPayloadBytes,
                publicKey = CoseVectors.devicePublicKey
            )
        )

        assertThat(result, equalTo(CoseVerificationResult.KeyBased))
        verify(exactly = 0) { spyHeaderValidator.validate(any()) }
        verify(exactly = 0) { spyPathValidator.verify(any(), any()) }
        verify(exactly = 0) { spyProfileValidator.validate(any(), any()) }
    }

    @Test
    fun `key-based malformed array throws MalformedCoseSign1`() {
        val request = CoseVerificationRequest.KeyBased(
            coseSign1Bytes = byteArrayOf(0x83.toByte()),
            detachedPayload = CoseVectors.keyBasedPayloadBytes,
            publicKey = CoseVectors.devicePublicKey
        )
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `key-based attached payload element throws MalformedCoseSign1`() {
        val request = CoseVerificationRequest.KeyBased(
            coseSign1Bytes = CoseVectors.createAttachedVector(),
            detachedPayload = CoseVectors.keyBasedPayloadBytes,
            publicKey = CoseVectors.devicePublicKey
        )
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `key-based unsupported algorithm throws UnsupportedAlgorithm`() {
        val request = CoseVerificationRequest.KeyBased(
            coseSign1Bytes = CoseVectors.createKeyBasedVector(alg = -35L),
            detachedPayload = CoseVectors.keyBasedPayloadBytes,
            publicKey = CoseVectors.devicePublicKey
        )
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `key-based non-P256 public key throws UnsupportedAlgorithm`() {
        val p384Key = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp384r1")) }
            .generateKeyPair().public as ECPublicKey

        val request = CoseVerificationRequest.KeyBased(
            coseSign1Bytes = CoseVectors.createKeyBasedVector(),
            detachedPayload = CoseVectors.keyBasedPayloadBytes,
            publicKey = p384Key
        )
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `key-based tampered signature throws InvalidSignature`() {
        val request = CoseVerificationRequest.KeyBased(
            coseSign1Bytes = CoseVectors.createKeyBasedVector(tamperSignature = true),
            detachedPayload = CoseVectors.keyBasedPayloadBytes,
            publicKey = CoseVectors.devicePublicKey
        )
        assertThrows(CoseVerificationFailure.InvalidSignature::class.java) {
            verifier.verify(request)
        }
    }
}
