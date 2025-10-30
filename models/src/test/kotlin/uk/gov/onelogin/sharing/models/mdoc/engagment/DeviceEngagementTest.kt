package uk.gov.onelogin.sharing.models.mdoc.engagment

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import tools.jackson.databind.json.JsonMapper
import uk.gov.onelogin.sharing.models.DeviceEngagementStub.DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.models.DeviceEngagementStub.ENGAGEMENT_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.models.DeviceEngagementStub.deviceEngagementNodes
import uk.gov.onelogin.sharing.models.InvalidDeviceEngagementStub.INVALID_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMappers

class DeviceEngagementTest {

    @Test
    fun `encode DeviceEngagement to expected base64 string`() {
        val encoded = DEVICE_ENGAGEMENT.encode()
        val base64 = Base64.getEncoder().encodeToString(encoded)
        assertEquals(ENGAGEMENT_EXPECTED_BASE_64, base64)
    }

    @Test
    fun `encode DeviceEngagement to expected json structure`() {
        val mapper = CborMappers.default()
        val cborBytes = DEVICE_ENGAGEMENT.encode()
        val actualNode = mapper.readTree(cborBytes)

        val expectedDeviceEngagement = deviceEngagementNodes()

        assertEquals(
            "CBOR structure should match expected JSON",
            expectedDeviceEngagement,
            actualNode
        )

        val json = JsonMapper.builder().build()
        val pretty = json.writerWithDefaultPrettyPrinter().writeValueAsString(actualNode)
        println(pretty)
    }

    @Test
    fun `ensure DeviceEngagement builder fails when retrieval methods is not provided`() {
        assertThrows(ExceptionInInitializerError::class.java) {
            INVALID_DEVICE_ENGAGEMENT.encode()
        }
    }
}
