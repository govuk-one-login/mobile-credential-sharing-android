package uk.gov.onelogin.sharing.models.mdoc.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import java.io.OutputStream

/**
 * Writes pre-encoded CBOR bytes directly into the output stream with no tag or bstr wrapper.
 *
 * Used for [issuerAuth] (COSE_Sign1 array, starts with 0x84) which must be spliced inline
 * per ISO 18013-5, not wrapped in a byte string.
 */
data class RawCbor(val encoded: ByteArray)

class RawCborSerializer : StdSerializer<RawCbor>(RawCbor::class.java) {
    override fun serialize(value: RawCbor, gen: JsonGenerator, provider: SerializerProvider) {
        val cborGen = gen as? CBORGenerator ?: error("RawCbor requires CBORGenerator")
        cborGen.flush()
        (cborGen.outputTarget as OutputStream).write(value.encoded)
    }
}
