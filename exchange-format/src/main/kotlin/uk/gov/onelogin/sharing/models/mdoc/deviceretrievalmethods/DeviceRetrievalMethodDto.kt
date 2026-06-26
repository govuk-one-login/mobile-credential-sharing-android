package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

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
import java.nio.ByteBuffer
import java.util.UUID
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable

@JsonSerialize(using = DeviceRetrievalMethodDto.Serializer::class)
@JsonDeserialize(using = DeviceRetrievalMethodDto.Deserializer::class)
data class DeviceRetrievalMethodDto(val type: Int, val version: Int, val options: BleOptionsDto) :
    CborEncodable {
    fun getPeripheralServerModeUuidString(): String? = options.getPeripheralServerModeUuidString()
    fun getPeripheralServerModeUuid(): UUID? = options.peripheralServerModeUuid?.toUUID()

    class Serializer :
        StdSerializer<DeviceRetrievalMethodDto>(DeviceRetrievalMethodDto::class.java) {
        override fun serialize(
            value: DeviceRetrievalMethodDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartArray(value, ELEMENT_COUNT)
            gen.writeNumber(value.type)
            gen.writeNumber(value.version)
            provider.defaultSerializeValue(value.options, gen)
            gen.writeEndArray()
        }
    }

    class Deserializer :
        StdDeserializer<DeviceRetrievalMethodDto>(DeviceRetrievalMethodDto::class.java) {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): DeviceRetrievalMethodDto {
            val root = p.codec.readTree<JsonNode>(p)
            val type = root[TYPE_INDEX].intValue()
            val version = root[VERSION_INDEX].intValue()
            val options = p.codec.treeToValue(root[OPTIONS_INDEX], BleOptionsDto::class.java)
            return DeviceRetrievalMethodDto(type, version, options)
        }
    }

    companion object {
        private const val ELEMENT_COUNT = 3
        const val TYPE_INDEX = 0
        const val VERSION_INDEX = 1
        const val OPTIONS_INDEX = 2
    }
}

fun BleDeviceRetrievalMethod.toDto(): DeviceRetrievalMethodDto = DeviceRetrievalMethodDto(
    type = type,
    version = version,
    options = options.toDto()
)

private fun ByteArray.toUUID(): UUID {
    val buffer = ByteBuffer.wrap(this)
    val high = buffer.getLong()
    val low = buffer.getLong()
    return UUID(high, low)
}
