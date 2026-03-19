package uk.gov.onelogin.sharing.cryptoService.engagement

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.DeviceEngagementStub.DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.DeviceEngagementStub.ENGAGEMENT_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.cryptoService.DeviceEngagementStub.deviceEngagementNodes
import uk.gov.onelogin.sharing.cryptoService.DeviceEngagementStub.deviceEngagementSerializers
import uk.gov.onelogin.sharing.cryptoService.InvalidDeviceEngagementStub.INVALID_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor

class DeviceEngagementTest {

    @Test
    fun `encode DeviceEngagement to expected base64 string`() {
        val encoded = DEVICE_ENGAGEMENT.encodeCbor()
        val base64 = Base64.getEncoder().encodeToString(encoded)
        assertEquals(ENGAGEMENT_EXPECTED_BASE_64, base64)
    }

    @Test
    fun `encode DeviceEngagement to expected json structure`() {
        val mapper = CborMapper.create(deviceEngagementSerializers)
        val cborBytes = DEVICE_ENGAGEMENT.encodeCbor()
        val actualNode = mapper.readTree(cborBytes)

        val expectedDeviceEngagement = deviceEngagementNodes()

        assertEquals(
            "CBOR structure should match expected JSON",
            expectedDeviceEngagement,
            actualNode
        )
    }

    @Test
    fun `ensure DeviceEngagement builder fails when retrieval methods is not provided`() {
        assertThrows(ExceptionInInitializerError::class.java) {
            INVALID_DEVICE_ENGAGEMENT.encodeCbor()
        }
    }
}
