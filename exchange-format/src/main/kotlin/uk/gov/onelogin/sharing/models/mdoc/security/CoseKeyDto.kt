package uk.gov.onelogin.sharing.models.mdoc.security

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable

@JsonSerialize(using = CoseKeyDto.Serializer::class)
data class CoseKeyDto(val keyType: Long, val curve: Long, val x: ByteArray, val y: ByteArray) :
    CborEncodable {
    class Serializer : StdSerializer<CoseKeyDto>(CoseKeyDto::class.java) {
        override fun serialize(
            value: CoseKeyDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
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

    companion object {
        private const val FIELD_COUNT = 4
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
