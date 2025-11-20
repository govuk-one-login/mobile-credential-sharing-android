package uk.gov.onelogin.sharing.security.cbor

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.security.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_CBOR

class DecoderTest {

    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()
    private val originalOut = System.out
    private val originalErr = System.err

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
        decodeDeviceEngagement(VALID_CBOR)

        val actualOutput = outContent.toString()
        assertTrue(actualOutput.contains("Successfully deserialized DeviceEngagementDto:"))
    }

    @Test
    fun `decodes device engagement from base64 url`() {
        decodeDeviceEngagement(INVALID_CBOR)
        val actualErrorMessage = outContent.toString()
        assertTrue(actualErrorMessage.contains("Failed to deserialize CBOR:"))
    }
}
