package uk.gov.onelogin.sharing.security

import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.databind.node.ObjectNode
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security
import uk.gov.onelogin.sharing.security.BleRetrievalStub.UUID_16_BIT
import uk.gov.onelogin.sharing.security.BleRetrievalStub.bleOptionNodes
import uk.gov.onelogin.sharing.security.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.security.SecurityTestStub.securityNodes
import uk.gov.onelogin.sharing.security.cbor.serializers.BleOptionsSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.CoseKeySerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.DeviceEngagementSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.DeviceRetrievalMethodSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.SecuritySerializer
import uk.gov.onelogin.sharing.security.cose.CoseKey

object DeviceEngagementStub {
    val deviceEngagementSerializers: Map<Class<*>, StdSerializer<*>> = mapOf(
        DeviceEngagement::class.java to DeviceEngagementSerializer(),
        DeviceRetrievalMethod::class.java to DeviceRetrievalMethodSerializer(),
        BleOptions::class.java to BleOptionsSerializer(),
        Security::class.java to SecuritySerializer(),
        EmbeddedCbor::class.java to EmbeddedCborSerializer(),
        CoseKey::class.java to CoseKeySerializer()
    )

    const val ENGAGEMENT_EXPECTED_BASE_64 =
        "vwBjMS4wAZ8B2BhQRkFLRV9FREVWSUNFX0tFWf8Cn58CAb8A9QH0ClARERERIiIzM0REVVVVVVVV/////w=="

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
