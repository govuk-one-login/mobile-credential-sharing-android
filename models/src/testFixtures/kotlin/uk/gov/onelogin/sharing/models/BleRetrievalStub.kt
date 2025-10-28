package uk.gov.onelogin.sharing.models

import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.databind.node.ObjectNode
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions

object BleRetrievalStub {
    const val BLE_EXPECTED_BASE_64 =
        "nwIBv2Ew9WEx9GIxMNgYWCQxMTExMTExMS0yMjIyLTMzMzMtNDQ0NC01NTU1NTU1NTU1NTX//w=="
    const val BLE_OPTIONS_EXPECTED_BASE_64 =
        "v2Ew9WEx9GIxMNgYWCQxMTExMTExMS0yMjIyLTMzMzMtNDQ0NC01NTU1NTU1NTU1NTX/"
    const val UUID = "11111111-2222-3333-4444-555555555555"

    private val jsonNodeFactory: JsonNodeFactory = JsonNodeFactory.instance

    val BLE_OPTIONS = BleOptions(
        serverMode = true,
        clientMode = false,
        peripheralServerModeUuid = EmbeddedCbor(UUID.toByteArray())
    )
    val BLE_RETRIEVAL_METHOD_SERVER_MODE =
        BleDeviceRetrievalMethod(
            type = BLE_TYPE,
            version = BLE_VERSION,
            options = BLE_OPTIONS
        )

    fun bleOptionNodes(
        serverMode: Boolean = true,
        clientMode: Boolean = false,
        uuid: String = UUID
    ): ObjectNode = jsonNodeFactory.objectNode()
        .put("0", serverMode)
        .put("1", clientMode)
        .put("10", uuid.toByteArray())
}
