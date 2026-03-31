package uk.gov.onelogin.sharing.cryptoService.verifier

import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_TRANSCRIPT
import uk.gov.onelogin.sharing.cryptoService.SessionEstablishmentStub.MOCK_E_READER_KEY
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor

class VerifierCryptoServiceImplTest {
    private val logger = SystemLogger()
    private val service = VerifierCryptoServiceImpl(logger)

    @Test
    fun `processEngagement constructs SessionTranscriptBytes successfully`() = runTest {
        val eReaderKeyTagged = MOCK_E_READER_KEY.hexToByteArray()

        val result = service.processEngagement(
            VALID_ENCODED_DEVICE_ENGAGEMENT,
            eReaderKeyTagged
        )

        val expectedTranscriptBytes =
            EmbeddedCbor(VALID_TRANSCRIPT.hexToByteArray()).encodeCbor()
        assertEquals(expectedTranscriptBytes.toHexString(), result.toHexString())

        assert("SessionTranscriptBytes constructed successfully" in logger)
    }

    @Test
    fun `processEngagement throws when DeviceEngagementBytes is blank`() = runTest {
        val eReaderKeyTagged = MOCK_E_READER_KEY.hexToByteArray()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.processEngagement("", eReaderKeyTagged)
        }

        assertEquals("DeviceEngagementBytes must not be blank", exception.message)
        assert(
            "error constructing SessionTranscript array due to " +
                "DeviceEngagementBytes is blank" in logger
        )
    }

    @Test
    fun `processEngagement throws when eReaderKeyTagged is not Tag 24`() = runTest {
        val invalidEReaderKey = byteArrayOf(0x00, 0x01, 0x02)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.processEngagement(VALID_ENCODED_DEVICE_ENGAGEMENT, invalidEReaderKey)
        }

        assertEquals("CBOR parsing error: eReaderKey must be tag(24)", exception.message)
    }
}
