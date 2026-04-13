package uk.gov.onelogin.sharing.cryptoService.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import uk.gov.onelogin.sharing.cryptoService.cbor.CborPropertyIds.PROPERTY_ID_0
import uk.gov.onelogin.sharing.cryptoService.cbor.CborPropertyIds.PROPERTY_ID_1
import uk.gov.onelogin.sharing.cryptoService.cbor.CborPropertyIds.PROPERTY_ID_2
import uk.gov.onelogin.sharing.models.mdoc.engagment.DeviceEngagement

class DeviceEngagementSerializer :
    StdSerializer<DeviceEngagement>(
        DeviceEngagement::class.java
    ) {
    override fun serialize(
        value: DeviceEngagement,
        gen: JsonGenerator,
        provider: SerializerProvider
    ) {
        (gen as CBORGenerator).writeStartObject(FIELD_COUNT)
        gen.writeFieldId(PROPERTY_ID_0)
        gen.writeString(value.version)
        gen.writeFieldId(PROPERTY_ID_1)
        provider.defaultSerializeValue(value.security, gen)
        gen.writeFieldId(PROPERTY_ID_2)
        gen.writeStartArray(value.deviceRetrievalMethods, value.deviceRetrievalMethods.size)
        value.deviceRetrievalMethods.forEach {
            provider.defaultSerializeValue(it, gen)
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }

    private companion object {
        const val FIELD_COUNT = 3
    }
}
