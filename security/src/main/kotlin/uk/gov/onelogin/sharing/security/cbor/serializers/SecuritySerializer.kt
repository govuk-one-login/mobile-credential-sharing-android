package uk.gov.onelogin.sharing.security.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.security.Security

class SecuritySerializer : StdSerializer<Security>(Security::class.java) {
    override fun serialize(value: Security, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartArray()
        gen.writeNumber(value.cipherSuiteIdentifier)
        val taggedBytes = EmbeddedCbor(value.eDeviceKeyBytes)
        provider.defaultSerializeValue(taggedBytes, gen)
        gen.writeEndArray()
    }
}
