package uk.gov.onelogin.sharing.cryptoService.verifier

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.secureArea.keypair.EcKeyPairGenerator

class VerifierCryptoServiceImplTest {
    private val logger = SystemLogger()
    private val service = VerifierCryptoServiceImpl(
        logger = logger,
        keyPairGenerator = EcKeyPairGenerator(logger)
    )

    @Test
    fun `processEngagement constructs SessionTranscriptBytes successfully`() = runTest {
        val result = service.processEngagement(VALID_ENCODED_DEVICE_ENGAGEMENT)

        assertNotNull(result.sessionTranscriptBytes)
        assertNotNull(result.eReaderKeyTagged)
        assertTrue(result.eReaderKeyTagged[0] == 0xD8.toByte())
        assertTrue(result.eReaderKeyTagged[1] == 0x18.toByte())

        assert("SessionTranscriptBytes constructed successfully" in logger)
    }

    @Test
    fun `processEngagement throws when DeviceEngagementBytes is blank`() = runTest {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            service.processEngagement("")
        }

        assertEquals("DeviceEngagementBytes must not be blank", exception.message)
        assert(
            "error constructing SessionTranscript array due to " +
                "DeviceEngagementBytes is blank" in logger
        )
    }
}
