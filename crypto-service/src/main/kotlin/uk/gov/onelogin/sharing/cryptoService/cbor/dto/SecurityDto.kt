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
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG
import uk.gov.onelogin.sharing.models.mdoc.security.Security

/**
 * Serialization: writes [eDeviceKeyBytes] directly as Tag 24 (same as the original SecuritySerializer).
 * Deserialization: unwraps Tag 24, parses inner CBOR as [CoseKeyDto] for key extraction.
 */
@JsonSerialize(using = SecurityDto.Serializer::class)
@JsonDeserialize(using = SecurityDto.Deserializer::class)
data class SecurityDto(
    val cipherSuiteIdentifier: Int,
    val eDeviceKeyBytes: ByteArray,
    val ephemeralPublicKey: CoseKeyDto? = null
) : CborEncodable {
    class Serializer : StdSerializer<SecurityDto>(SecurityDto::class.java) {
        override fun serialize(
            value: SecurityDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartArray(value, ELEMENT_COUNT)
            gen.writeNumber(value.cipherSuiteIdentifier)
            gen.writeTag(EMBEDDED_CBOR_TAG)
            gen.writeBinary(value.eDeviceKeyBytes)
            gen.writeEndArray()
        }

        private companion object {
            const val ELEMENT_COUNT = 2
        }
    }

    class Deserializer : JsonDeserializer<SecurityDto>() {
        private val cborFactory = CBORFactory()

        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SecurityDto {
            val root = p.codec.readTree<JsonNode>(p)
            val cipherSuiteIdentifier = root[0].intValue()
            val eDeviceKeyBytes = root[1].binaryValue()

            val parser = cborFactory.createParser(eDeviceKeyBytes).apply {
                codec = CborMapper.default
            }
            // skip Tag 24 and move to next element
            parser.nextToken()
            parser.nextToken()

            val coseNode = parser.codec.readTree<JsonNode>(parser)
            val cose = CoseKeyDto(
                keyType = coseNode[CoseKeyDto.KEY_TYPE_KEY].longValue(),
                curve = coseNode[CoseKeyDto.CURVE_KEY].longValue(),
                x = coseNode[CoseKeyDto.X_KEY].binaryValue(),
                y = coseNode[CoseKeyDto.Y_KEY].binaryValue()
            )
            return SecurityDto(
                cipherSuiteIdentifier = cipherSuiteIdentifier,
                eDeviceKeyBytes = eDeviceKeyBytes,
                ephemeralPublicKey = cose
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SecurityDto
        if (cipherSuiteIdentifier != other.cipherSuiteIdentifier) return false
        if (!eDeviceKeyBytes.contentEquals(other.eDeviceKeyBytes)) return false
        if (ephemeralPublicKey != other.ephemeralPublicKey) return false
        return true
    }

    override fun hashCode(): Int {
        var result = cipherSuiteIdentifier
        result = 31 * result + eDeviceKeyBytes.contentHashCode()
        result = 31 * result + (ephemeralPublicKey?.hashCode() ?: 0)
        return result
    }
}

fun Security.toDto(): SecurityDto = SecurityDto(
    cipherSuiteIdentifier = cipherSuiteIdentifier,
    eDeviceKeyBytes = eDeviceKeyBytes
)
