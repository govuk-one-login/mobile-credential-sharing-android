package uk.gov.onelogin.sharing.cryptoService.cbor.deserializers

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.BLE_RETRIEVAL_METHOD_SERVER_MODE
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceRetrievalMethodDto
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.toDto

class DeviceRetrievalMethodsDeserializerTest {

    @Test
    fun `maps DeviceRetrievalMethodDto via CborMapper`() {
        val dto = BLE_RETRIEVAL_METHOD_SERVER_MODE.toDto()
        val bytes = CborMapper.default.writeValueAsBytes(dto)
        val actual = CborMapper.default.readValue(bytes, DeviceRetrievalMethodDto::class.java)

        assertEquals(dto.type, actual.type)
        assertEquals(dto.version, actual.version)
        assertEquals(dto.options.serverMode, actual.options.serverMode)
        assertEquals(dto.options.clientMode, actual.options.clientMode)
        assertArrayEquals(
            dto.options.peripheralServerModeUuid,
            actual.options.peripheralServerModeUuid
        )
    }
}
