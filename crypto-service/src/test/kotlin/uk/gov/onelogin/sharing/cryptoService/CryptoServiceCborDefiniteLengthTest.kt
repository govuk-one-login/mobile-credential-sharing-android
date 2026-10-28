package uk.gov.onelogin.sharing.cryptoService

import java.security.Signature
import java.security.cert.Certificate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSigStructureGenerator
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSign1ProtectedHeaders
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.CoseSign1UnprotectedHeaderGenerator
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.ECReaderAuthProvider
import uk.gov.onelogin.sharing.cryptoService.verifier.reader.auth.SigningSignatureStructure
import uk.gov.onelogin.sharing.verification.cose.internal.path.CertificateStubs
import uk.gov.onelogin.sharing.verification.cose.internal.path.TestCertificateGenerator

/**
 * Enforces that all CBOR generators in the crypto-service module (including COSE_Sign1,
 * ReaderAuth headers, and Sig_Structure payloads) strictly emit definite-length CBOR.
 */
class CryptoServiceCborDefiniteLengthTest {

    private val logger = MemorisedLogger()

    private val certificateGenerator = TestCertificateGenerator(
        subject = "CN=Leaf,ST=London",
        keyPair = CertificateStubs.leafKeyPair,
        issuerKeyPair = CertificateStubs.rootKeyPair,
        issuer = "CN=Intermediate,ST=London"
    )

    private val leafCertificate: Certificate by lazy {
        certificateGenerator.leaf().build()
    }

    private val certificateChain: List<Certificate> by lazy {
        listOf(
            leafCertificate,
            certificateGenerator.build()
        )
    }

    private val privateKey by lazy {
        CertificateStubs.leafKeyPair.private
    }

    private val readerAuthProvider by lazy {
        ECReaderAuthProvider(
            logger = logger,
            certificateChain = certificateChain.map { it as java.security.cert.X509Certificate },
            protectedHeaderGenerator = CoseSign1ProtectedHeaders(logger),
            unprotectedHeaderGenerator = CoseSign1UnprotectedHeaderGenerator(logger),
            sigStructureGenerator = SigningSignatureStructure(
                logger = logger,
                signature = Signature.getInstance("SHA256withECDSA"),
                privateKey = privateKey,
                decorated = CoseSigStructureGenerator(
                    logger = logger,
                    protectedHeaderGenerator = CoseSign1ProtectedHeaders(logger)
                )
            )
        )
    }

    @Test
    fun `ECReaderAuthProvider produces COSE_Sign1 with strict definite-length CBOR`() = runTest {
        val payload = byteArrayOf(0x01, 0x02, 0x03, 0x04)
        val coseSign1Bytes = readerAuthProvider.sign(payload)

        assertDefiniteLengthCbor(coseSign1Bytes, "ECReaderAuthProvider COSE_Sign1")
    }

    @Test
    fun `Protected headers generator produces strict definite-length CBOR`() {
        val generator = CoseSign1ProtectedHeaders(logger)
        val bytes = generator.generateProtectedHeaders(leafCertificate).second

        assertDefiniteLengthCbor(bytes, "CoseSign1ProtectedHeaders")
    }

    @Test
    fun `Unprotected headers generator produces strict definite-length CBOR`() {
        val generator = CoseSign1UnprotectedHeaderGenerator(logger)
        val bytes = generator.generateUnprotectedHeaders(certificateChain).second

        assertDefiniteLengthCbor(bytes, "CoseSign1UnprotectedHeaderGenerator")
    }

    private fun assertDefiniteLengthCbor(bytes: ByteArray, description: String) {
        assertTrue("$description must not be empty", bytes.isNotEmpty())
        val walker = CborHeaderWalker(bytes)
        walker.walkAndAssertDefinite(description)
    }

    private class CborHeaderWalker(private val bytes: ByteArray) {
        private var offset = 0

        fun walkAndAssertDefinite(description: String) {
            walkItem(description)
            assertFalse(
                "Trailing unparsed bytes in $description at offset $offset / ${bytes.size}",
                offset < bytes.size
            )
        }

        private fun walkItem(description: String) {
            assertTrue(
                "Unexpected end of CBOR stream in $description at offset $offset",
                offset < bytes.size
            )
            val initial = bytes[offset].toInt() and 0xFF
            val majorType = initial ushr 5
            val addInfo = initial and 0x1F

            assertFalse(
                "Indefinite-length CBOR header found in" +
                    " $description at offset $offset: 0x${"%02X".format(initial)} " +
                    "(majorType=$majorType, addInfo=$addInfo)",
                addInfo == 31
            )

            offset++

            val length = when {
                addInfo < 24 -> addInfo
                addInfo == 24 -> readBytes(1)
                addInfo == 25 -> readBytes(2)
                addInfo == 26 -> readBytes(4)
                addInfo == 27 -> readBytes(8)
                else -> error("Invalid additional info $addInfo at offset ${offset - 1}")
            }

            when (majorType) {
                0, 1, 7 -> {}

                2, 3 -> {
                    offset += length
                }

                4 -> {
                    repeat(length) {
                        walkItem(description)
                    }
                }

                5 -> {
                    repeat(length) {
                        walkItem(description)
                        walkItem(description)
                    }
                }

                6 -> {
                    walkItem(description)
                }
            }
        }

        private fun readBytes(count: Int): Int {
            var value = 0
            repeat(count) {
                assertTrue("Truncated length argument at offset $offset", offset < bytes.size)
                value = (value shl 8) or (bytes[offset].toInt() and 0xFF)
                offset++
            }
            return value
        }
    }
}
