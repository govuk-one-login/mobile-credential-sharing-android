package uk.gov.onelogin.sharing.models

import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.databind.node.ObjectNode
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions

/**
 * Dummy object to provide some content within the test fixtures source set.
 *
 * Remove once there's meaningful code here.
 */
object BleRetrievalData {
    const val UUID = "11111111-2222-3333-4444-555555555555"
    const val BLE_EXPECTED_BASE_64 =
        "v2Ew9WEx9GIxMNgYWCQxMTExMTExMS0yMjIyLTMzMzMtNDQ0NC01NTU1NTU1NTU1NTX/"

    val BLE_RETRIEVAL_METHOD_SERVER_MODE =
        BleDeviceRetrievalMethod(
            type = BLE_TYPE,
            version = BLE_VERSION,
            options = BleOptions(
                serverMode = true,
                clientMode = false,
                peripheralServerModeUuid = EmbeddedCbor(UUID.toByteArray())
            )
        )

    val JSON_NODE_FACTORY: JsonNodeFactory = JsonNodeFactory.instance
    val JSON_OPTIONS: ObjectNode = JSON_NODE_FACTORY.objectNode()
        .put("0", true)
        .put("1", false)
        .put("10", UUID.toByteArray())

    val EXPECTED_NODES: ArrayNode = JSON_NODE_FACTORY.arrayNode()
        .add(BLE_TYPE)
        .add(BLE_VERSION)
        .add(JSON_OPTIONS)

}
