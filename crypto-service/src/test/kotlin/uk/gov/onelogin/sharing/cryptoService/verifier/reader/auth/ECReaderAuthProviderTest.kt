package uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayOutputStream
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.Security
import java.security.Signature
import java.security.SignatureException
import java.security.interfaces.ECPrivateKey
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.internal.matchers.ThrowableCauseMatcher.hasCause
import org.junit.internal.matchers.ThrowableMessageMatcher.hasMessage
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.models.mdoc.exceptions.UnrecoverableError
import uk.gov.onelogin.sharing.verification.trust.CertificateStubs
import uk.gov.onelogin.sharing.verification.trust.TestCertificateGenerator

private const val COSE_SIGN1_ARRAY_SIZE = 4
private const val P256_RAW_SIGNATURE_SIZE = 64
private const val ES256_ALG_LABEL = 1
private const val ES256_ALG_VALUE = -7
private const val X5T_LABEL = 34
private const val SHA256_ALG_ID = -16
private const val SHA256_HASH_SIZE = 32
private const val X5CHAIN_LABEL = 33

class ECReaderAuthProviderTest {

    private val logger = SystemLogger()
    private val cborMapper = ObjectMapper(CBORFactory())

    private val leafCertificate = TestCertificateGenerator(
        subject = "CN=Leaf,ST=London",
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = "CN=Intermediate,ST=London"
    ).leaf().build()

    private val intermediateCertificate = TestCertificateGenerator(
        subject = "CN=Intermediate,ST=London",
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = "CN=Issuer,ST=London"
    ).build()

    private val certificateChain = listOf(leafCertificate, intermediateCertificate)

    // The private key matching the leaf certificate's public key, so the produced signature
    // verifies against the leaf certificate (AC4).
    private val privateKey: ECPrivateKey = CertificateStubs.leafKeyPair.private as ECPrivateKey

    private var signature: Signature = Signature.getInstance("SHA256withECDSA")

    private val provider by lazy {
        ECReaderAuthProvider(
            logger = logger,
            certificateChain = certificateChain,
            protectedHeaderGenerator = CoseSign1ProtectedHeaders(logger),
            unprotectedHeaderGenerator = CoseSign1UnprotectedHeaderGenerator(logger),
            sigStructureGenerator = SigningSignatureStructure(
                logger = logger,
                signature = signature,
                privateKey = privateKey,
                decorated = CoseSigStructureGenerator(
                    logger = logger,
                    protectedHeaderGenerator = CoseSign1ProtectedHeaders(logger)
                )
            )
        )
    }

    private val coseSign1: ByteArray by lazy {
        provider.sign(byteArrayOf(1, 2, 3, 4, 5))
    }

    private val coseSign1Tree: JsonNode by lazy {
        cborMapper.readTree(coseSign1)
    }

    @BeforeTest
    fun setUp() {
        Security.addProvider(BouncyCastleProvider())
    }

    @AfterTest
    fun tearDown() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
    }

    @Test
    fun `Wraps InvalidKeyExceptions in UnrecoverableError instances`() = runTest {
        val invalidKeyException = InvalidKeyException("This is a unit test")
        signature = mockk(relaxed = true)
        every { signature.initSign(privateKey) } throws invalidKeyException

        val throwable = kotlin.test.assertFails {
            provider.sign(byteArrayOf())
        }

        assertThat(
            throwable,
            allOf(
                instanceOf(UnrecoverableError::class.java),
                hasMessage(equalTo("Couldn't initialise signing with the provided Private Key.")),
                hasCause(equalTo(invalidKeyException))
            )
        )
    }

    @Test
    fun `Wraps SignatureExceptions in UnrecoverableError instances`() = runTest {
        val exception = SignatureException("This is a unit test")
        val input = byteArrayOf(1, 2, 3)
        signature = mockk(relaxed = true)
        every { signature.update(any<ByteArray>()) } throws exception

        val throwable = kotlin.test.assertFails {
            provider.sign(input)
        }

        assertThat(
            throwable,
            allOf(
                instanceOf(UnrecoverableError::class.java),
                hasMessage(
                    equalTo("Couldn't create signature from provided reader authentication bytes.")
                ),
                hasCause(equalTo(exception))
            )
        )
    }

    @Test
    fun `Successfully signs provided byte array`() = runTest {
        assertNotNull(coseSign1)
    }

    @Test
    fun `Logs a successfully created COSE_Sign1 structure`() = runTest {
        provider.sign(byteArrayOf(1, 2, 3, 4, 5))

        assertTrue {
            "Created COSE_Sign1 structure" in logger
        }
    }

    @Test
    fun `Assembles a definite-length four-element array`() = runTest {
        assertTrue(coseSign1Tree.isArray)
        assertEquals(COSE_SIGN1_ARRAY_SIZE, coseSign1Tree.size())
    }

    @Test
    fun `Is untagged and not wrapped in a byte string`() = runTest {
        // First byte of an untagged, definite-length 4-element CBOR array is 0x84
        // (major type 4 array, length 4). A tag or byte-string wrapper would differ.
        assertEquals(0x84.toByte(), coseSign1[0])
    }

    @Test
    fun `First element is the protected header bytes containing alg ES256 and x5t`() = runTest {
        val protectedHeaderBytes = coseSign1Tree[0].binaryValue()
        assertNotNull(protectedHeaderBytes)

        val headerTree = cborMapper.readTree(protectedHeaderBytes)
        assertEquals(ES256_ALG_VALUE, headerTree[ES256_ALG_LABEL.toString()].intValue())

        val x5t = headerTree[X5T_LABEL.toString()]
        assertTrue(x5t.isArray)
        assertEquals(SHA256_ALG_ID, x5t[0].intValue())

        val hashBytes = x5t[1].binaryValue()
        assertEquals(SHA256_HASH_SIZE, hashBytes.size)
        val expectedHash = MessageDigest.getInstance("SHA-256").digest(leafCertificate.encoded)
        assertTrue(hashBytes.contentEquals(expectedHash))
    }

    @Test
    fun `Second element is the unprotected header map with leaf-first x5chain`() = runTest {
        val unprotectedHeader = coseSign1Tree[1]
        assertTrue(unprotectedHeader.isObject)
        assertEquals(1, unprotectedHeader.size())

        val x5chain = unprotectedHeader[X5CHAIN_LABEL.toString()]
        assertTrue(x5chain.isArray)
        assertEquals(2, x5chain.size())

        assertTrue(x5chain[0].binaryValue().contentEquals(leafCertificate.encoded))
        assertTrue(x5chain[1].binaryValue().contentEquals(intermediateCertificate.encoded))
    }

    @Test
    fun `x5t leaf hash matches the first x5chain entry`() = runTest {
        val protectedHeaderBytes = coseSign1Tree[0].binaryValue()
        val hashBytes = cborMapper.readTree(protectedHeaderBytes)[X5T_LABEL.toString()][1]
            .binaryValue()
        val firstX5ChainEntry = coseSign1Tree[1][X5CHAIN_LABEL.toString()][0].binaryValue()

        val expectedHash = MessageDigest.getInstance("SHA-256").digest(firstX5ChainEntry)
        assertTrue(hashBytes.contentEquals(expectedHash))
    }

    @Test
    fun `Third element is explicit CBOR null for the detached payload`() = runTest {
        assertTrue(coseSign1Tree[2].isNull)
    }

    @Test
    fun `Fourth element is the raw 64-byte R S signature`() = runTest {
        val signatureBytes = coseSign1Tree[3].binaryValue()
        assertNotNull(signatureBytes)
        assertEquals(P256_RAW_SIGNATURE_SIZE, signatureBytes.size)
    }

    @Test
    fun `Produced COSE_Sign1 signature verifies against the leaf certificate key`() = runTest {
        // Independently re-derive the Sig_structure and verify the emitted raw (R || S)
        // signature against the leaf public key, proving the assembled structure is coherent.
        // SHA256withPLAIN-ECDSA (BouncyCastle) consumes the raw 64-byte COSE signature directly.
        val protectedHeaderBytes = coseSign1Tree[0].binaryValue()
        val readerAuthenticationPayload = byteArrayOf(1, 2, 3, 4, 5)

        val sigStructure = sigStructure(protectedHeaderBytes, readerAuthenticationPayload)
        val rawSignature = coseSign1Tree[3].binaryValue()

        val verifier = Signature.getInstance(
            "SHA256withPLAIN-ECDSA",
            BouncyCastleProvider.PROVIDER_NAME
        ).apply {
            initVerify(certificateChain.first().publicKey)
            update(sigStructure)
        }
        assertTrue(verifier.verify(rawSignature))
    }

    private fun sigStructure(
        protectedHeaderBytes: ByteArray,
        readerAuthenticationPayload: ByteArray
    ): ByteArray = ByteArrayOutputStream().also { out ->
        CBORFactory().createGenerator(out).use { gen ->
            gen.writeStartArray(null, COSE_SIGN1_ARRAY_SIZE)
            gen.writeString("Signature1")
            gen.writeBinary(protectedHeaderBytes)
            gen.writeBinary(ByteArray(0))
            gen.writeBinary(readerAuthenticationPayload)
            gen.writeEndArray()
        }
    }.toByteArray()
}
