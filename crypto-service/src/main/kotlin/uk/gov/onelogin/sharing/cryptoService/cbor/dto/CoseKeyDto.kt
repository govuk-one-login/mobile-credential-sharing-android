package uk.gov.onelogin.sharing.cryptoService.cbor.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable
import uk.gov.onelogin.sharing.cryptoService.cose.Cose
import uk.gov.onelogin.sharing.cryptoService.cose.CoseKey

@JsonSerialize(using = CoseKeyDto.Serializer::class)
data class CoseKeyDto(
    @JsonProperty(KEY_TYPE_KEY) val keyType: Long,
    @JsonProperty(CURVE_KEY) val curve: Long,
    @JsonProperty(X_KEY) val x: ByteArray,
    @JsonProperty(Y_KEY) val y: ByteArray
) : CborEncodable {
    class Serializer : StdSerializer<CoseKeyDto>(CoseKeyDto::class.java) {
        override fun serialize(
            value: CoseKeyDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldId(Cose.KEY_KTY_LABEL)
            provider.defaultSerializeValue(value.keyType, gen)
            gen.writeFieldId(Cose.EC_CURVE_LABEL)
            gen.writeNumber(value.curve)
            gen.writeFieldId(Cose.EC_X_COORDINATE_LABEL)
            gen.writeBinary(value.x)
            gen.writeFieldId(Cose.EC_Y_COORDINATE_LABEL)
            gen.writeBinary(value.y)
            gen.writeEndObject()
        }

        private companion object {
            const val FIELD_COUNT = 4
        }
    }

    companion object {
        const val KEY_TYPE_KEY = "1"
        const val CURVE_KEY = "-1"
        const val X_KEY = "-2"
        const val Y_KEY = "-3"
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

fun CoseKey.toDto(): CoseKeyDto = CoseKeyDto(
    keyType = keyType,
    curve = curve,
    x = x,
    y = y
)
