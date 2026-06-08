package uk.gov.onelogin.sharing.cryptoService.dto

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.DecoderStub
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceEngagementDto

class DeviceEngagementDtoTest {

    @Test
    fun `deserialize into DeviceEngagementDto from valid Base64 Url CBOR`() {
        val cborData = Base64.getUrlDecoder().decode(DecoderStub.VALID_ENCODED_DEVICE_ENGAGEMENT)
        val actual = CborMapper.default.readValue(cborData, DeviceEngagementDto::class.java)

        assertEquals(DecoderStub.validDeviceEngagementDto, actual)
    }
}
