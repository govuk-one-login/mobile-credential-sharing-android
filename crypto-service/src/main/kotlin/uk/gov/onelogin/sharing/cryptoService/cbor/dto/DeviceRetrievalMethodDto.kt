package uk.gov.onelogin.sharing.cryptoService.cbor.dto

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
import java.util.UUID
import uk.gov.onelogin.sharing.core.UUIDExtensions.toUUID
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod

@JsonSerialize(using = DeviceRetrievalMethodDto.Serializer::class)
@JsonDeserialize(using = DeviceRetrievalMethodDto.Deserializer::class)
data class DeviceRetrievalMethodDto(val type: Int, val version: Int, val options: BleOptionsDto) : CborEncodable {
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

        private companion object {
            const val ELEMENT_COUNT = 3
        }
    }

    class Deserializer : JsonDeserializer<DeviceRetrievalMethodDto>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): DeviceRetrievalMethodDto {
            val root = p.codec.readTree<JsonNode>(p)
            val type = root[0].intValue()
            val version = root[1].intValue()
            val optsNode = root[2]
            val options = BleOptionsDto(
                serverMode = optsNode["0"].booleanValue(),
                clientMode = optsNode["1"].booleanValue(),
                peripheralServerModeUuid = if (optsNode.has(
                        "10"
                    )
                ) {
                    optsNode["10"].binaryValue()
                } else {
                    null
                }
            )
            return DeviceRetrievalMethodDto(type, version, options)
        }
    }
}

fun BleDeviceRetrievalMethod.toDto(): DeviceRetrievalMethodDto = DeviceRetrievalMethodDto(
    type = type,
    version = version,
    options = options.toDto()
)
