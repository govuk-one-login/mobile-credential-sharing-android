package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable

@JsonSerialize(using = BleOptionsDto.Serializer::class)
@JsonDeserialize(using = BleOptionsDto.Deserializer::class)
data class BleOptionsDto(
    val serverMode: Boolean,
    val clientMode: Boolean,
    val peripheralServerModeUuid: ByteArray?
) : CborEncodable {
    fun getPeripheralServerModeUuidString(): String? = peripheralServerModeUuid?.decodeToString()

    class Serializer : StdSerializer<BleOptionsDto>(BleOptionsDto::class.java) {
        override fun serialize(
            value: BleOptionsDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldId(SERVER_MODE_KEY)
            gen.writeBoolean(value.serverMode)
            gen.writeFieldId(CLIENT_MODE_KEY)
            gen.writeBoolean(value.clientMode)
            gen.writeFieldId(PERIPHERAL_UUID_KEY)
            provider.defaultSerializeValue(value.peripheralServerModeUuid, gen)
            gen.writeEndObject()
        }
    }

    class Deserializer : StdDeserializer<BleOptionsDto>(BleOptionsDto::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BleOptionsDto {
            val root = p.codec.readTree<JsonNode>(p)
            return BleOptionsDto(
                serverMode = root[SERVER_MODE_KEY.toString()].booleanValue(),
                clientMode = root[CLIENT_MODE_KEY.toString()].booleanValue(),
                peripheralServerModeUuid = if (root.has(PERIPHERAL_UUID_KEY.toString())) {
                    root[PERIPHERAL_UUID_KEY.toString()].binaryValue()
                } else {
                    null
                }
            )
        }
    }

    companion object {
        private const val FIELD_COUNT = 3
        const val SERVER_MODE_KEY = 0L
        const val CLIENT_MODE_KEY = 1L
        const val PERIPHERAL_UUID_KEY = 10L
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
