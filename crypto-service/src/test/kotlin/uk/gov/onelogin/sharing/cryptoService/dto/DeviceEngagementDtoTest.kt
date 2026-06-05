package uk.gov.onelogin.sharing.cryptoService.dto

import java.util.Base64
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.DecoderStub
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDto

class DeviceEngagementDtoTest {

    @Test
    fun `deserialize into DeviceEngagementDto from valid Base64 Url CBOR`() {
        val cborData = Base64.getUrlDecoder().decode(DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT)

        val actualDto = CborMapper.default.readValue(cborData, DeviceEngagementDto::class.java)

        val expectedDto = DecoderStub.validDeviceEngagementDto

        assertEquals(expectedDto.version, actualDto.version)
        assertEquals(
            expectedDto.deviceRetrievalMethods.first().type,
            actualDto.deviceRetrievalMethods.first().type
        )
        assertEquals(
            expectedDto.deviceRetrievalMethods.first().version,
            actualDto.deviceRetrievalMethods.first().version
        )
        assertArrayEquals(
            expectedDto.deviceRetrievalMethods.first().options.peripheralServerModeUuid,
            actualDto.deviceRetrievalMethods.first().options.peripheralServerModeUuid
        )
        assertNotNull(actualDto.security.ephemeralPublicKey)
    }
}
