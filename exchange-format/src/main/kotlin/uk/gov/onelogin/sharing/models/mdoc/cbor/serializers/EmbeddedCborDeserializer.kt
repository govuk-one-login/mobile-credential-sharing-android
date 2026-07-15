package uk.gov.onelogin.sharing.models.mdoc.cbor.serializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * Deserializes a CBOR Tag 24 (encoded CBOR data item) envelope into an [EmbeddedCbor] instance.
 *
 * This is the inverse of [EmbeddedCborSerializer] which wraps bytes in Tag 24.
 *
 */
class EmbeddedCborDeserializer : StdDeserializer<EmbeddedCbor>(EmbeddedCbor::class.java) {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EmbeddedCbor {
        val bytes = p.binaryValue
        return EmbeddedCbor(bytes)
    }
}
