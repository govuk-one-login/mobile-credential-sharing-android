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

    private val trustedRoots = listOf(CertificateStubs.rootCa)

    @Test
    fun `attached issuer auth returns verified leaf and exact payload`() {
        val request = CoseVerificationRequest.Attached(CoseVectors.attachedMsoBytes, trustedRoots)

        val result = verifier.verify(request) as CoseVerificationResult.Attached

        assertThat(result.payload, equalTo(CoseVectors.attachedMsoPayloadBytes))
        assertThat(
            result.leafCertificate.subjectX500Principal.name,
            equalTo("ST=London,C=GB,CN=Leaf")
        )
    }

    @Test
    fun `attached malformed array throws MalformedCoseSign1`() {
        val request = CoseVerificationRequest.Attached(byteArrayOf(0x83.toByte()), trustedRoots)
        assertThrows(CoseVerificationFailure.MalformedCoseSign1::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `attached unsupported algorithm throws UnsupportedAlgorithm`() {
        val request = CoseVerificationRequest.Attached(
            CoseVectors.createAttachedVector(alg = -35L),
            trustedRoots
        )
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `attached missing x5chain throws MissingX5Chain`() {
        val request = CoseVerificationRequest.Attached(
            CoseVectors.createAttachedVector(includeX5chain = false),
            trustedRoots
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
        val request = CoseVerificationRequest.Attached(CoseVectors.attachedMsoBytes, listOf(untrustedRoot))
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(request)
        }
    }

    @Test
    fun `attached profile violation throws CertificateProfileViolation`() {
        val request = CoseVerificationRequest.Attached(
            CoseVectors.createAttachedVector(eku = "1.0.18013.5.1.6"),
            trustedRoots
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
            trustedRoots
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
            trustedRoots = trustedRoots
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
            trustedRoots = trustedRoots
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
            trustedRoots = trustedRoots
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
            trustedRoots = trustedRoots
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
            trustedRoots = trustedRoots
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
            trustedRoots = trustedRoots
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
            trustedRoots = trustedRoots
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
            trustedRoots = listOf(untrustedRoot)
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
            trustedRoots = trustedRoots
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
            trustedRoots = trustedRoots
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
                trustedRoots = trustedRoots
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
                    trustedRoots = listOf(differentRoot)
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

    @Test
    fun `C10 AC1 - attached and detached verification succeeds when valid against first root in trustedRoots`() {
        val untrustedRoot = TestCertificateGenerator(
            subject = "CN=Untrusted",
            keyPair = CertificateStubs.rootKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Untrusted"
        ).ca().build()

        val attachedReq = CoseVerificationRequest.Attached(
            coseSign1Bytes = CoseVectors.attachedMsoBytes,
            trustedRoots = listOf(CertificateStubs.rootCa, untrustedRoot)
        )
        val attachedResult = verifier.verify(attachedReq) as CoseVerificationResult.Attached
        assertThat(attachedResult.leafCertificate.subjectX500Principal.name, equalTo("ST=London,C=GB,CN=Leaf"))

        val detachedReq = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoots = listOf(CertificateStubs.rootCa, untrustedRoot)
        )
        val detachedResult = verifier.verify(detachedReq) as CoseVerificationResult.Detached
        assertThat(detachedResult.leafCertificate.subjectX500Principal.name, equalTo("ST=London,C=GB,CN=Reader"))
    }

    @Test
    fun `C10 AC2 - attached and detached verification succeeds when valid against second root in trustedRoots`() {
        val untrustedRoot = TestCertificateGenerator(
            subject = "CN=Untrusted",
            keyPair = CertificateStubs.rootKeyPair,
            issuerKeyPair = CertificateStubs.rootKeyPair,
            issuer = "CN=Untrusted"
        ).ca().build()

        val attachedReq = CoseVerificationRequest.Attached(
            coseSign1Bytes = CoseVectors.attachedMsoBytes,
            trustedRoots = listOf(untrustedRoot, CertificateStubs.rootCa)
        )
        val attachedResult = verifier.verify(attachedReq) as CoseVerificationResult.Attached
        assertThat(attachedResult.leafCertificate.subjectX500Principal.name, equalTo("ST=London,C=GB,CN=Leaf"))

        val detachedReq = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoots = listOf(untrustedRoot, CertificateStubs.rootCa)
        )
        val detachedResult = verifier.verify(detachedReq) as CoseVerificationResult.Detached
        assertThat(detachedResult.leafCertificate.subjectX500Principal.name, equalTo("ST=London,C=GB,CN=Reader"))
    }

    @Test
    fun `C10 AC3 - chain containing any supplied root fails with UntrustedCertificate before validation`() {
        val req1 = CoseVerificationRequest.Attached(
            coseSign1Bytes = CoseVectors.attachedMsoBytes,
            trustedRoots = listOf(CertificateStubs.leafSignedByRoot, CertificateStubs.rootCa)
        )
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(req1)
        }

        val req2 = CoseVerificationRequest.Attached(
            coseSign1Bytes = CoseVectors.attachedMsoBytes,
            trustedRoots = listOf(CertificateStubs.rootCa, CertificateStubs.leafSignedByRoot)
        )
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(req2)
        }
    }

    @Test
    fun `C10 AC4 - empty trustedRoots list or chain valid against no roots throws UntrustedCertificate`() {
        val emptyAttachedReq = CoseVerificationRequest.Attached(
            coseSign1Bytes = CoseVectors.attachedMsoBytes,
            trustedRoots = emptyList()
        )
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(emptyAttachedReq)
        }

        val emptyDetachedReq = CoseVerificationRequest.Detached(
            coseSign1Bytes = CoseVectors.createDetachedVector(),
            detachedPayload = CoseVectors.detachedReaderAuthPayloadBytes,
            trustedRoots = emptyList()
        )
        assertThrows(CoseVerificationFailure.UntrustedCertificate::class.java) {
            verifier.verify(emptyDetachedReq)
        }
    }

    @Test
    fun `C10 Short Circuit - non-untrusted failure on root A stops execution immediately`() {
        val unsupportedAlgReq = CoseVerificationRequest.Attached(
            coseSign1Bytes = CoseVectors.createAttachedVector(alg = -35L),
            trustedRoots = listOf(CertificateStubs.rootCa)
        )
        assertThrows(CoseVerificationFailure.UnsupportedAlgorithm::class.java) {
            verifier.verify(unsupportedAlgReq)
        }
    }
}
