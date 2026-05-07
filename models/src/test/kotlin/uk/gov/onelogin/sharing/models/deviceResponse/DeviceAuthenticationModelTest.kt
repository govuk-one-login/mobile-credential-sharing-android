package uk.gov.onelogin.sharing.models.deviceResponse

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceAuthentication

class DeviceAuthenticationModelTest {

    @Test
    fun `label defaults to DeviceAuthentication`() {
        val model = DeviceAuthentication(
            sessionTranscript = byteArrayOf(0x01),
            docType = "org.iso.18013.5.1.mDL",
            deviceNameSpacesBytes = byteArrayOf(0x02)
        )

        assertEquals(DeviceAuthentication.DEVICE_AUTHENTICATION, model.label)
    }

    @Test
    fun `stores sessionTranscript`() {
        val transcript = byteArrayOf(0x01, 0x02, 0x03)
        val model = DeviceAuthentication(
            sessionTranscript = transcript,
            docType = "org.iso.18013.5.1.mDL",
            deviceNameSpacesBytes = byteArrayOf()
        )

        assertArrayEquals(transcript, model.sessionTranscript)
    }

    @Test
    fun `stores docType`() {
        val model = DeviceAuthentication(
            sessionTranscript = byteArrayOf(),
            docType = "org.iso.18013.5.1.mDL",
            deviceNameSpacesBytes = byteArrayOf()
        )

        assertEquals("org.iso.18013.5.1.mDL", model.docType)
    }

    @Test
    fun `stores deviceNameSpacesBytes`() {
        val nameSpaces = byteArrayOf(0x0A, 0x0B)
        val model = DeviceAuthentication(
            sessionTranscript = byteArrayOf(),
            docType = "org.iso.18013.5.1.mDL",
            deviceNameSpacesBytes = nameSpaces
        )

        assertArrayEquals(nameSpaces, model.deviceNameSpacesBytes)
    }

    @Test
    fun `ELEMENT_COUNT is 4`() {
        assertEquals(4, DeviceAuthentication.ELEMENT_COUNT)
    }
}
