package uk.gov.onelogin.sharing.models.mdoc.security

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
import com.fasterxml.jackson.dataformat.cbor.CBORParser
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper

@JsonSerialize(using = CoseKeyDto.Serializer::class)
@JsonDeserialize(using = CoseKeyDto.Deserializer::class)
data class CoseKeyDto(val keyType: Long, val curve: Long, val x: ByteArray, val y: ByteArray) :
    CborEncodable {
    class Serializer : StdSerializer<CoseKeyDto>(CoseKeyDto::class.java) {
        override fun serialize(
            value: CoseKeyDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            require(value.keyType in VALID_KEY_TYPES) {
                "Invalid COSE key type: ${value.keyType}, " +
                    "expected one of $VALID_KEY_TYPES"
            }
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldId(KEY_TYPE_KEY)
            provider.defaultSerializeValue(value.keyType, gen)
            gen.writeFieldId(CURVE_KEY)
            gen.writeNumber(value.curve)
            gen.writeFieldId(X_KEY)
            gen.writeBinary(value.x)
            gen.writeFieldId(Y_KEY)
            gen.writeBinary(value.y)
            gen.writeEndObject()
        }
    }

    class Deserializer : StdDeserializer<CoseKeyDto>(CoseKeyDto::class.java) {
        override fun deserialize(parser: JsonParser, context: DeserializationContext?): CoseKeyDto =
            (parser as CBORParser).use { parser ->
                val rootNode = CborMapper.default.readTree<JsonNode>(parser)

                val keyType: Long = rootNode[KEY_TYPE_KEY.toString()].numberValue().toLong()
                require(keyType in VALID_KEY_TYPES) {
                    "Invalid COSE key type: $keyType, " +
                        "expected one of $VALID_KEY_TYPES"
                }
                val curve = rootNode[CURVE_KEY.toString()].numberValue().toLong()
                val x = rootNode[X_KEY.toString()].binaryValue()
                val y = rootNode[Y_KEY.toString()].binaryValue()

                CoseKeyDto(
                    keyType = keyType,
                    curve = curve,
                    x = x,
                    y = y
                )
            }
    }

    companion object {
        private const val FIELD_COUNT = 4
        private const val KEY_TYPE_OKP: Long = 1
        private const val KEY_TYPE_EC2: Long = 2
        private val VALID_KEY_TYPES: Set<Long> = setOf(KEY_TYPE_OKP, KEY_TYPE_EC2)
        const val KEY_TYPE_KEY: Long = 1
        const val CURVE_KEY: Long = -1
        const val X_KEY: Long = -2
        const val Y_KEY: Long = -3
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CoseKeyDto
        if (keyType != other.keyType) return false
        if (curve != other.curve) return false
        if (!x.contentEquals(other.x)) return false
        if (!y.contentEquals(other.y)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = keyType.hashCode()
        result = 31 * result + curve.hashCode()
        result = 31 * result + x.contentHashCode()
        result = 31 * result + y.contentHashCode()
        return result
    }
}
