package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SharingDeviceSignedTest {

    private val zeroToOne = byteArrayOf(0, 1)
    private val oneToTwo = byteArrayOf(1, 2)

    private val deviceSigned = SharingDeviceSigned(
        deviceNameSpacesBytes = zeroToOne,
        deviceSignature = oneToTwo
    )

    private val differentNameSpaces = deviceSigned.copy(
        deviceNameSpacesBytes = oneToTwo
    )

    private val differentSignature = deviceSigned.copy(
        deviceSignature = zeroToOne
    )

    @Suppress("EqualsNullCall")
    @Test
    fun `Equality contract`() {
        assertEquals(deviceSigned, deviceSigned)
        assertEquals(deviceSigned, deviceSigned.copy())

        assertFalse(deviceSigned.equals(null))
        assertFalse(deviceSigned.equals("different type"))
        assertNotEquals(deviceSigned, differentNameSpaces)
        assertNotEquals(deviceSigned, differentSignature)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(deviceSigned.hashCode(), deviceSigned.copy().hashCode())

        assertNotEquals(deviceSigned.hashCode(), differentNameSpaces.hashCode())
        assertNotEquals(deviceSigned.hashCode(), differentSignature.hashCode())
    }
}