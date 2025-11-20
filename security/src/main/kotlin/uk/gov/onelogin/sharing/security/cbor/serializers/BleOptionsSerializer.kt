package uk.gov.onelogin.sharing.security.cbor.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.security.cbor.CborPropertyIds.PROPERTY_ID_0
import uk.gov.onelogin.sharing.security.cbor.CborPropertyIds.PROPERTY_ID_1
import uk.gov.onelogin.sharing.security.cbor.CborPropertyIds.PROPERTY_ID_10

class BleOptionsSerializer : StdSerializer<BleOptions>(BleOptions::class.java) {
    override fun serialize(value: BleOptions, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeFieldId(PROPERTY_ID_0)
        gen.writeBoolean(value.serverMode)
        gen.writeFieldId(PROPERTY_ID_1)
        gen.writeBoolean(value.clientMode)
        gen.writeFieldId(PROPERTY_ID_10)
        provider.defaultSerializeValue(value.peripheralServerModeUuid, gen)
        gen.writeEndObject()
    }
}
