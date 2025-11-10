package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

import java.util.UUID

data class BleDeviceRetrievalMethod(
    override val type: Int = BLE_TYPE,
    override val version: Int = BLE_VERSION,
    override val options: BleOptions
) : DeviceRetrievalMethod {
    companion object {
        const val BLE_TYPE = 2
        const val BLE_VERSION = 1
    }
}

data class BleOptions(
    val serverMode: Boolean = true,
    val clientMode: Boolean = false,
    val peripheralServerModeUuid: ByteArray = UUID.randomUUID().toByteArray()
)
