package uk.gov.onelogin.sharing.cryptoService.engagement

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertFalse
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

    // Expected prefix taken from ISO 18013-5 Appendix D.3.1
    @Test
    fun `DeviceEngagement encodes to definite-length map`() {
        val encoded = DEVICE_ENGAGEMENT.encodeCbor()

        // D.3.1: DeviceEngagement starts with a3 (definite-length map, 3 entries)
        assertEquals("a3", encoded.toHexString().substring(0, 2))
    }

    // ISO 18013-5: no indefinite-length markers in encoded output
    @Test
    fun `DeviceEngagement contains no indefinite-length markers`() {
        val encoded = DEVICE_ENGAGEMENT.encodeCbor()
        val hex = encoded.toHexString()

        assertFalse("Contains indefinite-length array (9f)", hex.contains("9f"))
        assertFalse("Contains indefinite-length map (bf)", hex.contains("bf"))
    }
}
