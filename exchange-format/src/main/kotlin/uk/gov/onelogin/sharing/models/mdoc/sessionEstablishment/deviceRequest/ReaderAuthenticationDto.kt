package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import java.io.OutputStream
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor

/**
 * Represents the ReaderAuthentication structure as defined in ISO 18013-5.
 *
 * ```
 * ReaderAuthentication = [
 *     "ReaderAuthentication",
 *     SessionTranscript,
 *     ItemsRequestBytes
 * ]
 * ```
 */
@JsonSerialize(using = ReaderAuthenticationDto.Serializer::class)
data class ReaderAuthenticationDto(
    val sessionTranscript: ByteArray,
    val itemsRequestBytes: ByteArray
) : CborEncodable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReaderAuthenticationDto

        if (!sessionTranscript.contentEquals(other.sessionTranscript)) return false
        if (!itemsRequestBytes.contentEquals(other.itemsRequestBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sessionTranscript.contentHashCode()
        result = 31 * result + itemsRequestBytes.contentHashCode()
        return result
    }

    class Serializer : StdSerializer<ReaderAuthenticationDto>(ReaderAuthenticationDto::class.java) {
        override fun serialize(
            value: ReaderAuthenticationDto,
            gen: JsonGenerator,
            provider: SerializerProvider
        ) {
            val cborGen = gen as CBORGenerator
            cborGen.writeStartArray()
            cborGen.writeString(LABEL)
            cborGen.flush()
            val out = cborGen.outputTarget as OutputStream
            out.write(value.sessionTranscript)
            out.write(value.itemsRequestBytes)
            cborGen.writeEndArray()
        }
    }

    companion object {
        private const val LABEL = "ReaderAuthentication"

        /**
         * Builds Tag 24 wrapped ReaderAuthenticationBytes payload from the untagged active session transcript
         * and preserved ItemsRequestBytes as required by ISO/IEC 18013-5 Clause 8.3.2.1.
         */
        fun createReaderAuthenticationBytes(
            untaggedSessionTranscriptBytes: ByteArray,
            itemsRequestBytes: ByteArray
        ): ByteArray {
            val dto = ReaderAuthenticationDto(
                sessionTranscript = untaggedSessionTranscriptBytes,
                itemsRequestBytes = itemsRequestBytes
            )
            return EmbeddedCbor(dto.toCbor()).toCbor()
        }
    }
}
