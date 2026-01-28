package uk.gov.onelogin.sharing.security.cbor

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.security.InvalidKeyException
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import junit.framework.TestCase.assertTrue
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.security.DecoderStub.validDeviceEngagementDto
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.MOCK_SESSION_ESTABLISHMENT_DATA
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.eReaderKeyHexFormat
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.expectedSessionEstablishmentDto
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.invalidCborMissingDataParameter
import uk.gov.onelogin.sharing.security.SessionEstablishmentStub.invalidCborMissingEReader
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidKeyPair
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.generateValidUnsupportedKeyPair
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub.getSharedSecret
import uk.gov.onelogin.sharing.security.cose.CoseKey
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_ALGORITHM
import uk.gov.onelogin.sharing.security.cryptography.Constants.ELLIPTIC_CURVE_PARAMETER_SPEC
import uk.gov.onelogin.sharing.security.toSessionEstablishment

class DecoderTest {

    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()
    private val originalOut = System.out
    private val originalErr = System.err
    private val logger = SystemLogger()

    @Before
    fun setUpStreams() {
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(errContent))
    }

    @After
    fun restoreStreams() {
        System.setOut(originalOut)
        System.setErr(originalErr)
    }

    @Test
    fun `successfully decodes device engagement from base64 url cbor`() {
        val result = decodeDeviceEngagement(
            cborBase64Url = VALID_ENCODED_DEVICE_ENGAGEMENT,
            logger = logger
        )

        val actualOutput = outContent.toString()
        assertTrue(actualOutput.contains("Successfully deserialized DeviceEngagementDto:"))

        assertEquals(
            validDeviceEngagementDto,
            result
        )
    }

    @Test
    fun `decodes device engagement from base64 url`() {
        val result = decodeDeviceEngagement(
            cborBase64Url = INVALID_CBOR,
            logger = logger
        )
        val actualErrorMessage = outContent.toString()
        assertTrue(actualErrorMessage.contains("Failed to deserialize CBOR:"))

        assertNull(result)
    }

    @Test
    fun `decodeSessionEstablishmentModel decodes raw bytes into SessionEstablishmentDto`() {
        val result = decodeSessionEstablishmentModel(
            MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray(),
            logger
        )

        assertArrayEquals(
            expectedSessionEstablishmentDto.eReaderKey.encoded,
            result.eReaderKey.encoded
        )
        assertArrayEquals(expectedSessionEstablishmentDto.data, result.data)
    }

    @Test
    fun `decodeSessionEstablishmentModel logs tagged eReaderKey in diagnostic hex format`() {
        decodeSessionEstablishmentModel(
            MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray(),
            logger
        )

        val actualReaderHexLog = outContent.toString()
        assertTrue(actualReaderHexLog.contains(eReaderKeyHexFormat))
    }

    @Test
    fun `decodeSessionEstablishmentModel with invalid CBOR throws exception`() {
        val malformedBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            decodeSessionEstablishmentModel(malformedBytes, logger)
        }

        assertTrue(exception is IllegalArgumentException)
        assert(exception.message!!.contains(CborErrors.DECODING_ERROR.errorMessage))
    }

    @Test
    fun `empty cbor throws exception when deserializing session establishment model bytes`() {
        val emptyMapCbor = byteArrayOf(0xA0.toByte())
        val exception = assertThrows(IllegalArgumentException::class.java) {
            decodeSessionEstablishmentModel(emptyMapCbor, logger)
        }

        assertTrue(exception is IllegalArgumentException)
        assert(exception.message!!.contains(CborErrors.PARSING_ERROR.errorMessage))
    }

    @Test
    fun `CBOR missing eReaderKey should throw session establishment status 12 exception`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            decodeSessionEstablishmentModel(invalidCborMissingEReader.hexToByteArray(), logger)
        }

        assertTrue(exception is IllegalArgumentException)
        assert(exception.message!!.contains(CborErrors.PARSING_ERROR.errorMessage))
    }

    @Test
    fun `CBOR missing data parameter should throw session establishment status 12 exception`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            decodeSessionEstablishmentModel(
                invalidCborMissingDataParameter.hexToByteArray(),
                logger
            )
        }

        assertTrue(exception is IllegalArgumentException)
        assert(exception.message!!.contains(CborErrors.PARSING_ERROR.errorMessage))
    }

    @Test
    fun `should log error for valid decoded cbor with incorrect engagementData parameters`() {
        val invalidDto = validDeviceEngagementDto.copy(
            deviceRetrievalMethods = emptyList()
        )

        val result = decodeDeviceEngagement(invalidDto.toString(), logger)

        val actualErrorMessage = outContent.toString()

        assertTrue(actualErrorMessage.contains("Illegal parameter"))
        assertNull(result)
    }

    @Test
    fun `should generate the same shared secret for for holder and verifier session`() {
        val holderSession = FakeSessionSecurity()
        val readerSession = FakeSessionSecurity()

        val readerKeyPair = readerSession.generateEcKeyPair(
            ELLIPTIC_CURVE_ALGORITHM,
            ELLIPTIC_CURVE_PARAMETER_SPEC
        )
        val holderKeyPair = holderSession.generateEcKeyPair(
            ELLIPTIC_CURVE_ALGORITHM,
            ELLIPTIC_CURVE_PARAMETER_SPEC
        )

        val readerPrivateKey = readerSession.getSessionPrivateKey()
        val holderPrivateKey = holderSession.getSessionPrivateKey()

        val eReaderSharedSecret = getSharedSecret(
            readerPrivateKey,
            holderKeyPair.public as ECPublicKey
        )

        val holderSharedSecret = getSharedSecret(
            holderPrivateKey,
            readerKeyPair.public as ECPublicKey
        )

        assertArrayEquals(eReaderSharedSecret, holderSharedSecret)
    }

    @Test
    fun `should throw error if unsupported curve used to create shared secret`() {
        val readerKeyPair = generateValidKeyPair()
        val holderKeyPair = generateValidUnsupportedKeyPair()

        assertThrows(InvalidKeyException::class.java) {
            getSharedSecret(
                holderKeyPair?.private as ECPrivateKey,
                readerKeyPair?.public as ECPublicKey
            )
        }
        val actualErrorMessage = outContent.toString()

        assert(actualErrorMessage.contains("Unable to create shared secret"))
    }

    @Test
    fun `should use eReaderKey from sessionEstablishment model to generate shared secret`() {
        val holderKeyPair = generateValidKeyPair()

        val sessionEstablishment = decodeSessionEstablishmentModel(
            MOCK_SESSION_ESTABLISHMENT_DATA.hexToByteArray(),
            logger
        ).toSessionEstablishment()

        val untagReaderKey = deriveUntaggedCbor(sessionEstablishment.eReaderKey)

        val eReaderKeyPublicKey = CoseKey.getEReaderKeyFromParsedCoseKey(untagReaderKey)

        val sharedSecret = getSharedSecret(
            holderKeyPair?.private as ECPrivateKey,
            eReaderKeyPublicKey
        )

        assertNotNull(sharedSecret)
    }
}
