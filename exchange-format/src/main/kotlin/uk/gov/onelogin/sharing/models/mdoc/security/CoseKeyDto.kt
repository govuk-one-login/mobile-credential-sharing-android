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
import uk.gov.onelogin.sharing.models.mdoc.cose.ECKeyType

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
                validateCurveMatchesKeyType(keyType, curve)
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
        private val VALID_KEY_TYPES: Set<Long> =
            setOf(ECKeyType.OKP.id.toLong(), ECKeyType.EC.id.toLong())
        const val KEY_TYPE_KEY: Long = 1
        const val CURVE_KEY: Long = -1
        const val X_KEY: Long = -2
        const val Y_KEY: Long = -3

        /**
         * Curves associated with key type EC2 (kty=2).
         */
        private val EC2_CURVES: Set<Long> = setOf(
            1L,
            2L,
            3L, // P-256, P-384, P-521
            256L,
            257L,
            258L,
            259L // Brainpool P-256, P-320, P-384, P-512
        )

        /**
         * Curves associated with key type OKP (kty=1).
         */
        private val OKP_CURVES: Set<Long> = setOf(
            4L,
            5L,
            6L,
            7L // X25519, X448, Ed25519, Ed448
        )

        private fun validateCurveMatchesKeyType(keyType: Long, curve: Long) {
            val expectedCurves = when (keyType) {
                KEY_TYPE_EC2 -> EC2_CURVES
                KEY_TYPE_OKP -> OKP_CURVES
                else -> return
            }
            require(curve in expectedCurves) {
                "Curve $curve is not valid for key type $keyType"
            }
        }
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
