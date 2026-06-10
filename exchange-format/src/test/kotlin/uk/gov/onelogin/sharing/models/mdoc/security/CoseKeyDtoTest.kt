package uk.gov.onelogin.sharing.models.mdoc.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CoseKeyDtoTest {

    private val x = byteArrayOf(
        -109, -68, 38, 41, 73, -111, -26, -81, -88, -51, -111, -127, -42, -29, -64,
        41, -8, -68, -70, -104, -94, 3, -62, 66, -13, -62, 37, -126, 15, 4, 106, -86
    )
    private val y = byteArrayOf(
        60, 66, 38, -6, 108, 39, -38, 106, -1, -71, 3, -60, 47, 124, 33, 28, 120, 4,
        -15, -44, 127, 71, 71, -14, -93, -39, -103, -51, 123, 44, 89, 45
    )

    @Test
    fun `equals should return true for instances with same values`() {
        val dto1 = CoseKeyDto(keyType = 2L, curve = -1L, x = x, y = y)
        val dto2 = CoseKeyDto(keyType = 2L, curve = -1L, x = x, y = y)
        assertEquals(dto1, dto2)
    }

    @Test
    fun `equals should return false for instances with different values`() {
        val dto1 = CoseKeyDto(keyType = 2L, curve = 1L, x = x, y = y)
        val dto2 = CoseKeyDto(keyType = 2L, curve = -1L, x = x, y = y)
        assertNotEquals(dto1, dto2)
    }

    @Test
    fun `hashCodes should be equal for instances`() {
        val dto1 = CoseKeyDto(keyType = 2L, curve = -1L, x = x, y = y)
        val dto2 = CoseKeyDto(keyType = 2L, curve = -1L, x = x, y = y)
        assertEquals(dto1.hashCode(), dto2.hashCode())
    }

    @Test
    fun `hashCodes should be different for non-equal instances`() {
        val dto1 = CoseKeyDto(keyType = 2L, curve = 1L, x = x, y = y)
        val dto2 = CoseKeyDto(keyType = 2L, curve = -1L, x = x, y = y)
        assertNotEquals(dto1.hashCode(), dto2.hashCode())
    }
}
