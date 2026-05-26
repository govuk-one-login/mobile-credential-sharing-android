package uk.gov.onelogin.sharing.models.mdoc.transcript

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HandoverTest {

    private val nfcHandover = Handover.NFC(
        selectMessage = byteArrayOf(0, 1),
        requestMessage = byteArrayOf(1, 2)
    )
    private val differentRequestMessage = nfcHandover.copy(
        requestMessage = byteArrayOf(2, 3)
    )

    private val differentSelectMessage = nfcHandover.copy(
        selectMessage = byteArrayOf(2, 3)
    )

    @Test
    fun `Equality contract`() {
        assertEquals(Handover.QR, Handover.QR)
        assertNotEquals(Handover.QR, nfcHandover)

        assertEquals(nfcHandover, nfcHandover.copy())
        assertNotEquals(nfcHandover, differentRequestMessage)
        assertNotEquals(nfcHandover, differentSelectMessage)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(Handover.QR.hashCode(), Handover.QR.hashCode())
        assertNotEquals(Handover.QR.hashCode(), nfcHandover.hashCode())

        assertEquals(nfcHandover.hashCode(), nfcHandover.copy().hashCode())
        assertNotEquals(nfcHandover.hashCode(), differentRequestMessage.hashCode())
        assertNotEquals(nfcHandover.hashCode(), differentSelectMessage.hashCode())
    }
}
