package uk.gov.onelogin.sharing.cryptoService.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable

data class EmbeddedCbor(val encoded: ByteArray) : CborEncodable

class EmbeddedCborSerializer : StdSerializer<EmbeddedCbor>(EmbeddedCbor::class.java) {
    override fun serialize(value: EmbeddedCbor, gen: JsonGenerator, provider: SerializerProvider) {
        val cborGen = gen as? CBORGenerator
            ?: error("EmbeddedCbor requires CBORGenerator")
        cborGen.writeTag(EMBEDDED_CBOR_TAG)
        cborGen.writeBinary(value.encoded)
    }

    companion object {
        const val EMBEDDED_CBOR_TAG = 24
    }
}
