package uk.gov.onelogin.sharing.models.mdoc.engagment

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagementDtoStub.VALID_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagementDtoStub.validDeviceEngagementDto

class DeviceEngagementDtoTest {

    @Test
    fun `deserialize into DeviceEngagementDto from valid Base64 Url CBOR`() {
        val cborData = Base64.getUrlDecoder().decode(VALID_ENCODED_DEVICE_ENGAGEMENT)
        val actual = CborMapper.default.readValue(cborData, DeviceEngagementDto::class.java)

        assertEquals(validDeviceEngagementDto, actual)
    }
}
