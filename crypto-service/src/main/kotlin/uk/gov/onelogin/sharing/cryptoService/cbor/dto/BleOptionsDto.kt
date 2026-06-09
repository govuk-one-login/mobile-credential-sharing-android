package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions

@JsonSerialize(using = BleOptionsDto.Serializer::class)
@JsonDeserialize(using = BleOptionsDto.Deserializer::class)
data class BleOptionsDto(
    @JsonProperty(SERVER_MODE_KEY) val serverMode: Boolean,
    @JsonProperty(CLIENT_MODE_KEY) val clientMode: Boolean,
    @JsonProperty(PERIPHERAL_UUID_KEY) val peripheralServerModeUuid: ByteArray?
) : CborEncodable {
    fun getPeripheralServerModeUuidString(): String? = peripheralServerModeUuid?.decodeToString()

    class Serializer : StdSerializer<BleOptionsDto>(BleOptionsDto::class.java) {
        override fun serialize(
            value: BleOptionsDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldId(SERVER_MODE_ID)
            gen.writeBoolean(value.serverMode)
            gen.writeFieldId(CLIENT_MODE_ID)
            gen.writeBoolean(value.clientMode)
            gen.writeFieldId(PERIPHERAL_UUID_ID)
            provider.defaultSerializeValue(value.peripheralServerModeUuid, gen)
            gen.writeEndObject()
        }

        private companion object {
            const val FIELD_COUNT = 3
            const val SERVER_MODE_ID = 0L
            const val CLIENT_MODE_ID = 1L
            const val PERIPHERAL_UUID_ID = 10L
        }
    }

    class Deserializer : JsonDeserializer<BleOptionsDto>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BleOptionsDto {
            val root = p.codec.readTree<JsonNode>(p)
            return BleOptionsDto(
                serverMode = root[SERVER_MODE_KEY].booleanValue(),
                clientMode = root[CLIENT_MODE_KEY].booleanValue(),
                peripheralServerModeUuid = if (root.has(PERIPHERAL_UUID_KEY)) {
                    root[PERIPHERAL_UUID_KEY].binaryValue()
                } else {
                    null
                }
            )
        }
    }

    companion object {
        const val SERVER_MODE_KEY = "0"
        const val CLIENT_MODE_KEY = "1"
        const val PERIPHERAL_UUID_KEY = "10"
        const val SERVER_MODE_ID = 0L
        const val CLIENT_MODE_ID = 1L
        const val PERIPHERAL_UUID_ID = 10L
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BleOptionsDto
        if (serverMode != other.serverMode) return false
        if (clientMode != other.clientMode) return false
        if (peripheralServerModeUuid != null) {
            if (other.peripheralServerModeUuid == null) return false
            if (!peripheralServerModeUuid.contentEquals(
                    other.peripheralServerModeUuid
                )
            ) {
                return false
            }
        } else if (other.peripheralServerModeUuid != null) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        var result = serverMode.hashCode()
        result = 31 * result + clientMode.hashCode()
        result = 31 * result + (peripheralServerModeUuid?.contentHashCode() ?: 0)
        return result
    }
}

fun BleOptions.toDto(): BleOptionsDto = BleOptionsDto(
    serverMode = serverMode,
    clientMode = clientMode,
    peripheralServerModeUuid = peripheralServerModeUuid
)
