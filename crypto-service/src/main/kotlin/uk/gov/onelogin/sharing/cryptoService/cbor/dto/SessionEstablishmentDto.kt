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
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable
import uk.gov.onelogin.sharing.cryptoService.cbor.CborErrors
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer.Companion.EMBEDDED_CBOR_TAG

@RequiresImplementation(
    details = [
        ImplementationDetail(
            ticket = "DCMAW-16759",
            description = "Build DeviceRequest and nested models"
        )
    ]
)
@JsonSerialize(using = SessionEstablishmentDto.Serializer::class)
@JsonDeserialize(using = SessionEstablishmentDto.Deserializer::class)
data class SessionEstablishmentDto(val eReaderKey: EmbeddedCbor, val data: ByteArray) :
    CborEncodable {
    class Serializer : StdSerializer<SessionEstablishmentDto>(SessionEstablishmentDto::class.java) {
        override fun serialize(
            value: SessionEstablishmentDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
            gen.writeFieldId(E_READER_KEY_ID)
            gen.writeTag(EMBEDDED_CBOR_TAG)
            gen.writeBinary(value.eReaderKey.encoded)
            gen.writeFieldId(DATA_ID)
            gen.writeBinary(value.data)
            gen.writeEndObject()
        }

        private companion object {
            const val FIELD_COUNT = 2
            const val E_READER_KEY_ID = 1L
            const val DATA_ID = 10L
        }
    }

    class Deserializer : JsonDeserializer<SessionEstablishmentDto>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): SessionEstablishmentDto {
            val root = p.codec.readTree<JsonNode>(p)

            require(root.isObject) {
                CborErrors.DECODING_ERROR.errorMessage
            }

            val eReaderKeyNode = root[E_READER_KEY_KEY] ?: root[E_READER_KEY_FALLBACK]
            requireNotNull(eReaderKeyNode) {
                CborErrors.PARSING_ERROR.errorMessage
            }

            val dataNode = root[DATA_KEY] ?: root[DATA_FALLBACK]
            requireNotNull(dataNode) {
                CborErrors.PARSING_ERROR.errorMessage
            }

            val eReaderKeyBytes = eReaderKeyNode.binaryValue()
            val dataBytes = dataNode.binaryValue()

            return SessionEstablishmentDto(
                eReaderKey = EmbeddedCbor(eReaderKeyBytes),
                data = dataBytes
            )
        }
    }

    companion object {
        const val E_READER_KEY_KEY = "1"
        const val DATA_KEY = "10"
        const val E_READER_KEY_FALLBACK = "eReaderKey"
        const val DATA_FALLBACK = "data"
        const val E_READER_KEY_ID = 1L
        const val DATA_ID = 10L
    }
}
