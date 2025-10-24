package uk.gov.onelogin.sharing.models.mdoc.engagment

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMappers
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.security.Security

data class DeviceEngagement(
    val version: String,
    val security: Security,
    val deviceRetrievalMethods: List<DeviceRetrievalMethod>
) {
    companion object {
        fun builder() = Builder()
    }

    class Builder {
        private var version: String = "1.0"
        private var security: Security? = null
        private val retrievalMethods = mutableListOf<DeviceRetrievalMethod>()

        fun version(v: String) = apply { version = v }

        fun security(keyBytes: ByteArray, cipherSuiteId: Int) = apply {
            security = Security(
                cipherSuiteIdentifier = cipherSuiteId,
                eDeviceKeyBytes = EmbeddedCbor(keyBytes)
            )
        }

        fun ble(serverMode: Boolean = true, clientMode: Boolean = false, peripheralUuid: String) =
            apply {
                val options = BleOptions(
                    serverMode = serverMode,
                    clientMode = clientMode,
                    peripheralServerModeUuid = EmbeddedCbor(peripheralUuid.toByteArray())
                )
                retrievalMethods.add(BleDeviceRetrievalMethod(options = options))
            }

        fun build(): DeviceEngagement {
            requireNotNull(security) {
                "Security must be provided"
            }
            require(retrievalMethods.isNotEmpty()) {
                "At least one retrieval method required"
            }
            return DeviceEngagement(
                version = version,
                security = security!!,
                deviceRetrievalMethods = retrievalMethods
            )
        }
    }
}

class DeviceEngagementSerializer :
    StdSerializer<DeviceEngagement>(
        DeviceEngagement::class.java
    ) {
    override fun serialize(
        value: DeviceEngagement,
        gen: JsonGenerator,
        provider: SerializationContext
    ) {
        gen.writeStartObject()
        gen.writeName("0")
        gen.writeString(value.version)
        gen.writeName("1")
        provider.writeValue(gen, value.security)
        gen.writeName("2")
        gen.writeStartArray()
        value.deviceRetrievalMethods.forEach {
            provider.writeValue(gen, it)
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }
}

object DeviceEngagementCbor {
    val mapper: ObjectMapper = CborMappers.default()

    fun encode(deviceEngagement: DeviceEngagement): ByteArray =
        mapper.writeValueAsBytes(deviceEngagement)
}
