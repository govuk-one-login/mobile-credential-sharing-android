package uk.gov.onelogin.sharing.models.mdoc.transcript

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security

class SessionTranscriptTest {

    private val nullTranscript = SessionTranscript(
        deviceEngagement = null,
        eReaderKey = null,
        handover = null
    )

    private val differentEngagement = nullTranscript.copy(
        deviceEngagement = DeviceEngagement(
            version = "unit test",
            security = Security(
                cipherSuiteIdentifier = 0,
                eDeviceKeyBytes = byteArrayOf(1, 2)
            ),
            deviceRetrievalMethods = listOf()
        )
    )

    private val differentKey = nullTranscript.copy(
        eReaderKey = byteArrayOf(1, 2)
    )

    private val differentHandover = nullTranscript.copy(
        handover = Handover.QR
    )

    @Test
    fun `Equality contract`() {
        assertEquals(nullTranscript, nullTranscript.copy())
        assertNotEquals(nullTranscript, differentEngagement)
        assertNotEquals(nullTranscript, differentKey)
        assertNotEquals(nullTranscript, differentHandover)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(nullTranscript.hashCode(), nullTranscript.copy().hashCode())
        assertNotEquals(nullTranscript.hashCode(), differentEngagement.hashCode())
        assertNotEquals(nullTranscript.hashCode(), differentKey.hashCode())
        assertNotEquals(nullTranscript.hashCode(), differentHandover.hashCode())
    }
}
