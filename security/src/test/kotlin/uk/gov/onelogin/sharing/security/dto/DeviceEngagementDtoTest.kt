package uk.gov.onelogin.sharing.security.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.Base64
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import uk.gov.onelogin.sharing.security.DecoderStub
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceEngagementDto

class DeviceEngagementDtoTest {

    @Test
    fun `deserialize into DeviceEngagementDto from valid Base64 Url CBOR`() {
        val cborFactory = CBORFactory()
        val cborMapper = ObjectMapper(cborFactory)

        val cborData = Base64.getUrlDecoder().decode(DecoderStub.VALID_CBOR)

        val actualDto: DeviceEngagementDto = cborMapper.readValue(cborData)

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
