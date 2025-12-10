package uk.gov.onelogin.sharing.security.cbor

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import junit.framework.TestCase.assertTrue
import kotlin.test.assertNull
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.security.DecoderStub.INVALID_CBOR
import uk.gov.onelogin.sharing.security.DecoderStub.VALID_CBOR
import uk.gov.onelogin.sharing.security.DecoderStub.validDeviceEngagementDto

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
            cborBase64Url = VALID_CBOR,
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
}
