package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

import java.util.UUID
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ser.std.StdSerializer
import uk.gov.onelogin.sharing.models.mdoc.EmbeddedCbor

data class BleDeviceRetrievalMethod(
    override val type: Int = 2,
    override val version: Int = 1,
    override val options: BleOptions = BleOptions()
) : DeviceRetrievalMethod

data class BleOptions(
    val serverMode: Boolean = true,
    val clientMode: Boolean = false,
    val peripheralServerModeUuid: EmbeddedCbor = EmbeddedCbor(
        UUID.randomUUID().toString().toByteArray()
    )
)

class BleOptionsSerializer : StdSerializer<BleOptions>(BleOptions::class.java) {
    override fun serialize(value: BleOptions, gen: JsonGenerator, provider: SerializationContext) {
        gen.writeStartObject()
        gen.writeName("0")
        gen.writeBoolean(value.serverMode)
        gen.writeName("1")
        gen.writeBoolean(value.clientMode)
        gen.writeName("10")
        provider.writeValue(gen, value.peripheralServerModeUuid)
        gen.writeEndObject()
    }
}
