package uk.gov.onelogin.sharing.cryptoService

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.UUID_16_BIT
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.bleOptionNodes
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.securityNodes
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement

object DeviceEngagementStub {
    const val ENGAGEMENT_EXPECTED_BASE_64 =
        "owBjMS4wAYIB2BhQRkFLRV9FREVWSUNFX0tFWQKBgwIBowD1AfQKUBEREREiIjMzRERVVVVVVVU="

    private val jsonNodeFactory: JsonNodeFactory = JsonNodeFactory.instance

    private fun deviceEngagementBuilder(): DeviceEngagement.Builder =
        DeviceEngagement.builder(SECURITY)
            .version("1.0")
            .ble(peripheralUuid = UUID_16_BIT)

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
        val drmsArray = jsonNodeFactory.arrayNode()
        deviceRetrievalMethods.forEach { drmsArray.add(it) }
        return jsonNodeFactory.objectNode().apply {
            put("0", version)
            set<ArrayNode>("1", securityNode)
            set<ArrayNode>("2", drmsArray)
        }
    }
}

object InvalidDeviceEngagementStub {
    private fun invalidDeviceEngagementBuilder(): DeviceEngagement.Builder =
        DeviceEngagement.builder(SECURITY)

    val INVALID_DEVICE_ENGAGEMENT: DeviceEngagement =
        invalidDeviceEngagementBuilder().build()
}
