package uk.gov.onelogin.sharing.cryptoService.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.models.mdoc.security.Security

class SecuritySerializer : StdSerializer<Security>(Security::class.java) {
    override fun serialize(value: Security, gen: JsonGenerator, provider: SerializerProvider) {
        (gen as CBORGenerator).writeStartArray(value, ELEMENT_COUNT)
        gen.writeNumber(value.cipherSuiteIdentifier)
        val taggedBytes = EmbeddedCbor(value.eDeviceKeyBytes)
        provider.defaultSerializeValue(taggedBytes, gen)
        gen.writeEndArray()
    }

    private companion object {
        const val ELEMENT_COUNT = 2
    }
}
