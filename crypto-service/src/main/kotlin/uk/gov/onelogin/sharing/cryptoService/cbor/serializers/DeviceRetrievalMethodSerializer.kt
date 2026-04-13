package uk.gov.onelogin.sharing.cryptoService.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod

class DeviceRetrievalMethodSerializer :
    StdSerializer<DeviceRetrievalMethod>(DeviceRetrievalMethod::class.java) {
    override fun serialize(
        value: DeviceRetrievalMethod,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        (gen as CBORGenerator).writeStartArray(value, ELEMENT_COUNT)
        gen.writeNumber(value.type)
        gen.writeNumber(value.version)
        provider.defaultSerializeValue(value.options, gen)
        gen.writeEndArray()
    }

    private companion object {
        const val ELEMENT_COUNT = 3
    }
}
