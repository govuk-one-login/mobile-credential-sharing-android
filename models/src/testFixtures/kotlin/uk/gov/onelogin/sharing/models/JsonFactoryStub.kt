package uk.gov.onelogin.sharing.models

import tools.jackson.databind.node.ArrayNode
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.databind.node.ObjectNode
import uk.gov.onelogin.sharing.models.MdocStubStrings.FAKE_CIPHER_ID
import uk.gov.onelogin.sharing.models.MdocStubStrings.FAKE_EDEVICE_KEY
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION

object JsonFactoryStub {
    private val jsonNodeFactory: JsonNodeFactory = JsonNodeFactory.instance

    fun bleOptionNodes(
        serverMode: Boolean = true,
        clientMode: Boolean = false,
        uuid: String = MdocStubStrings.UUID
    ): ObjectNode = jsonNodeFactory.objectNode()
        .put("0", serverMode)
        .put("1", clientMode)
        .put("10", uuid.toByteArray())

    fun deviceRetrievalNodes(
        type: Int = BLE_TYPE,
        version: Int = BLE_VERSION,
        options: ObjectNode = bleOptionNodes()
    ): ArrayNode = jsonNodeFactory.arrayNode()
        .add(type)
        .add(version)
        .add(options)

    fun securityNodes(
        cipherId: Int = FAKE_CIPHER_ID,
        keyBytes: ByteArray = FAKE_EDEVICE_KEY.toByteArray()
    ): ArrayNode = jsonNodeFactory.arrayNode()
        .add(cipherId)
        .add(keyBytes)

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
