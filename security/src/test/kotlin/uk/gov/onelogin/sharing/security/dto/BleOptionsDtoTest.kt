package uk.gov.onelogin.sharing.security.dto

import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray
import uk.gov.onelogin.sharing.security.BleRetrievalStub.UUID_16_BIT
import uk.gov.onelogin.sharing.security.cbor.dto.BleOptionsDto

class BleOptionsDtoTest {

    @Test
    fun `equals should return true for instances with same values`() {
        val dto1 = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = UUID_16_BIT.toByteArray()
        )
        val dto2 = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = UUID_16_BIT.toByteArray()

        )
        assertEquals(dto1, dto2)
    }

    @Test
    fun `equals should return false for instances with different values`() {
        val dto1 = BleOptionsDto(
            serverMode = false,
            clientMode = false,
            peripheralServerModeUuid = UUID_16_BIT.toByteArray()
        )
        val dto2 = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = UUID.randomUUID().toByteArray()

        )
        assertNotEquals(dto1, dto2)
    }

    @Test
    fun `hashCodes should be equal for instances`() {
        val dto1 = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = UUID_16_BIT.toByteArray()
        )
        val dto2 = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = UUID_16_BIT.toByteArray()

        )
        assertEquals(dto1.hashCode(), dto2.hashCode())
    }

    @Test
    fun `hashCodes should be different for non-equal instances`() {
        val dto1 = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = UUID_16_BIT.toByteArray()
        )
        val dto2 = BleOptionsDto(
            serverMode = true,
            clientMode = false,
            peripheralServerModeUuid = UUID.randomUUID().toByteArray()

        )
        assertNotEquals(dto1.hashCode(), dto2.hashCode())
    }
}
