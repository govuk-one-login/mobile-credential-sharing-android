package uk.gov.onelogin.sharing.cryptoService

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.UUID
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod.Companion.BLE_VERSION
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray

object BleRetrievalStub {
    const val BLE_EXPECTED_BASE_64 = "gwIBowD1AfQKUBEREREiIjMzRERVVVVVVVU="
    const val BLE_OPTIONS_EXPECTED_BASE_64 = "owD1AfQKUBEREREiIjMzRERVVVVVVVU="
    const val UUID_STRING = "11111111-2222-3333-4444-555555555555"
    val UUID_16_BIT: UUID = UUID.fromString(UUID_STRING)

    private val jsonNodeFactory: JsonNodeFactory = JsonNodeFactory.instance

    val BLE_OPTIONS = BleOptions(
        serverMode = true,
        clientMode = false,
        peripheralServerModeUuid = UUID_16_BIT.toByteArray()
    )

    val BLE_RETRIEVAL_METHOD_SERVER_MODE =
        BleDeviceRetrievalMethod(
            type = BLE_TYPE,
            version = BLE_VERSION,
            options = BLE_OPTIONS
        )

    // ISO 18013-5 Appendix D.3.1
    val D_3_1_BLE_OPTIONS = BleOptions(
        serverMode = false,
        clientMode = true,
        peripheralServerModeUuid = "45efef742b2c4837a9a3b0e1d05a6917".hexToByteArray()
    )

    const val D_3_1_BLE_OPTIONS_HEX = "a300f401f50a5045efef742b2c4837a9a3b0e1d05a6917"

    fun bleOptionNodes(
        serverMode: Boolean = true,
        clientMode: Boolean = false,
        uuid: ByteArray = UUID_16_BIT.toByteArray()
    ): ObjectNode = jsonNodeFactory.objectNode()
        .put("0", serverMode)
        .put("1", clientMode)
        .put("10", uuid)
}
