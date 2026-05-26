package uk.gov.onelogin.sharing.models.mdoc.engagment

import java.util.UUID
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toByteArray
import uk.gov.onelogin.sharing.models.mdoc.security.Security

data class DeviceEngagement(
    val version: String,
    val security: Security,
    val deviceRetrievalMethods: List<DeviceRetrievalMethod>
) {
    companion object {
        fun builder(security: Security): Builder = Builder(security)
    }

    class Builder(private val security: Security) {
        private var version: String = "1.0"
        private val retrievalMethods = mutableListOf<DeviceRetrievalMethod>()

        fun version(v: String) = apply { version = v }

        fun ble(serverMode: Boolean = true, clientMode: Boolean = false, peripheralUuid: UUID) =
            apply {
                val options = BleOptions(
                    serverMode = serverMode,
                    clientMode = clientMode,
                    peripheralServerModeUuid = peripheralUuid.toByteArray()
                )
                retrievalMethods.add(BleDeviceRetrievalMethod(options = options))
            }

        fun build(): DeviceEngagement {
            require(retrievalMethods.isNotEmpty()) {
                "At least one retrieval method required"
            }
            return DeviceEngagement(
                version = version,
                security = security,
                deviceRetrievalMethods = retrievalMethods
            )
        }
    }
}
