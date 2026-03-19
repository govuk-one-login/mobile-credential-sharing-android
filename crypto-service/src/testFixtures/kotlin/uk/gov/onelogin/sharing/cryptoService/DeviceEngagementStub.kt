package uk.gov.onelogin.sharing.cryptoService

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.UUID_16_BIT
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.bleOptionNodes
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.securityNodes
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.BleOptionsSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.CoseKeySerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.DeviceEngagementSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.DeviceRetrievalMethodSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.SecuritySerializer
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement
import uk.gov.onelogin.sharing.models.mdoc.security.Security

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
        val drmsArray = jsonNodeFactory.arrayNode()

        deviceRetrievalMethods.forEach { drm ->
            drmsArray.add(drm)
        }

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
