package uk.gov.onelogin.sharing.models.mdoc

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import tools.jackson.dataformat.cbor.CBORGenerator

@JvmInline
value class EmbeddedCbor(val encoded: ByteArray)

class EmbeddedCborSerializer : StdSerializer<EmbeddedCbor>(EmbeddedCbor::class.java) {
    override fun serialize(
        value: EmbeddedCbor,
        gen: JsonGenerator,
        provider: SerializationContext
    ) {
        val cborGen = gen as? CBORGenerator
            ?: error("EmbeddedCbor requires CBORGenerator")
        cborGen.writeTag(EMBEDDED_CBOR_TAG)
        cborGen.writeBinary(value.encoded)
    }

    companion object {
        const val EMBEDDED_CBOR_TAG = 24
    }
}
