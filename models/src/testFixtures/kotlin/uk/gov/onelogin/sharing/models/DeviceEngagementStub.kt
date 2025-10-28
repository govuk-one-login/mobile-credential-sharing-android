package uk.gov.onelogin.sharing.models

import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.databind.node.ObjectNode
import uk.gov.onelogin.sharing.models.BleRetrievalStub.UUID
import uk.gov.onelogin.sharing.models.BleRetrievalStub.bleOptionNodes
import uk.gov.onelogin.sharing.models.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.models.SecurityTestStub.securityNodes
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement

object DeviceEngagementStub {
    const val ENGAGEMENT_EXPECTED_BASE_64 =
        "v2EwYzEuMGExnwHYGFBGQUtFX0VERVZJQ0VfS0VZ/2Eyn58CAb9hMPVhMfRiMTDYGFgkM" +
            "TExMTExMTEtMjIyMi0zMzMzLTQ0NDQtNTU1NTU1NTU1NTU1/////w=="

    private val jsonNodeFactory: JsonNodeFactory = JsonNodeFactory.instance
    private fun deviceEngagementBuilder(): DeviceEngagement.Builder =
        DeviceEngagement.builder(SECURITY)
            .version("1.0")
            .ble(peripheralUuid = UUID)

    val DEVICE_ENGAGEMENT: DeviceEngagement = deviceEngagementBuilder().build()

    fun deviceRetrievalNodes(
        type: Int = BLE_TYPE,
        version: Int = BLE_VERSION,
        options: ObjectNode = bleOptionNodes()
    ): ArrayNode = jsonNodeFactory.arrayNode()
        .add(type)
        .add(version)
        .add(options)

    fun deviceEngagementNodes(
        version: String = "1.0",
        securityNode: ArrayNode = securityNodes(),
        deviceRetrievalMethods: List<ArrayNode> = listOf(deviceRetrievalNodes())
    ): ObjectNode {
        val drmsArray = jsonNodeFactory.arrayNode().addAll(deviceRetrievalMethods)

        return jsonNodeFactory.objectNode()
            .put("0", version)
            .set("1", securityNode)
            .set("2", drmsArray)
    }
}

object InvalidDeviceEngagementStub {
    private fun invalidDeviceEngagementBuilder(): DeviceEngagement.Builder =
        DeviceEngagement.builder(SECURITY)

    val INVALID_DEVICE_ENGAGEMENT: DeviceEngagement =
        invalidDeviceEngagementBuilder().build()
}
