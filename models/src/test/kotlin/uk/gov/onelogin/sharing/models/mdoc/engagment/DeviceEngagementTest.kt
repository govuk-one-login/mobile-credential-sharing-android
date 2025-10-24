package uk.gov.onelogin.sharing.models.mdoc.engagment

import java.util.Base64
import junit.framework.TestCase.assertEquals
import kotlin.test.assertEquals
import org.junit.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.node.JsonNodeFactory
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMappers
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION

class DeviceEngagementTest {
    private val uuid = "11111111-2222-3333-4444-555555555555"
    private val fakeKeyBytes = "FAKE_EDEVICE_KEY".toByteArray()

    private val deviceEngagement = DeviceEngagement.builder()
        .version("1.0")
        .security(fakeKeyBytes, 1)
        .ble(peripheralUuid = uuid)
        .build()

    @Test
    fun `encode DeviceEngagement to expected base64 string`() {
        val encoded = DeviceEngagementCbor.encode(deviceEngagement)
        val base64 = Base64.getEncoder().encodeToString(encoded)

        val expectedBase64 =
            "v2EwYzEuMGExnwHYGFBGQUtFX0VERVZJQ0VfS0VZ/2Eyn58CAb9hMPVhMfRiMTDYGFgkM" +
                "TExMTExMTEtMjIyMi0zMzMzLTQ0NDQtNTU1NTU1NTU1NTU1/////w=="
        assertEquals(expectedBase64, base64)
    }

    @Test
    fun `encode DeviceEngagement to expected json structure`() {
        val fakeCipherId = 1

        val mapper = CborMappers.default()
        val cborBytes = DeviceEngagementCbor.encode(deviceEngagement)
        val actualNode = mapper.readTree(cborBytes)

        val jsonNodeFactory = JsonNodeFactory.instance

        val expectedOptions = jsonNodeFactory.objectNode()
            .put("0", true)
            .put("1", false)
            .put("10", uuid.toByteArray())

        val expectedDrm = jsonNodeFactory.arrayNode()
            .add(BLE_TYPE)
            .add(BLE_VERSION)
            .add(expectedOptions)

        val expectedDrms = jsonNodeFactory.arrayNode().add(expectedDrm)

        val expectedSecurity = jsonNodeFactory.arrayNode()
            .add(fakeCipherId)
            .add(fakeKeyBytes)

        val expectedEngagement = jsonNodeFactory.objectNode()
            .put("0", "1.0")
            .set("1", expectedSecurity)
            .set("2", expectedDrms)

        assertEquals(
            expectedEngagement,
            actualNode,
            "CBOR structure should match expected JSON"
        )

        val json = JsonMapper.builder().build()
        val pretty = json.writerWithDefaultPrettyPrinter().writeValueAsString(actualNode)
        println(pretty)
    }
}
