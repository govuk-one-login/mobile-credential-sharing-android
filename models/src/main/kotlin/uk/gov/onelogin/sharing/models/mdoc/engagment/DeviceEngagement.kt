package uk.gov.onelogin.sharing.models.mdoc.engagment

import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer
import tools.jackson.dataformat.cbor.CBORFactory
import tools.jackson.dataformat.cbor.CBORMapper
import tools.jackson.module.kotlin.KotlinModule
import uk.gov.onelogin.sharing.models.mdoc.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptionsSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethodSerializer
import uk.gov.onelogin.sharing.models.mdoc.security.Security
import uk.gov.onelogin.sharing.models.mdoc.security.SecuritySerializer

data class DeviceEngagement(
    val version: String,
    val security: Security,
    val deviceRetrievalMethods: List<DeviceRetrievalMethod>
)

class DeviceEngagementSerializer :
    StdSerializer<DeviceEngagement>(
        DeviceEngagement::class.java
    ) {
    override fun serialize(
        value: DeviceEngagement,
        gen: JsonGenerator,
        provider: SerializationContext
    ) {
        gen.writeStartObject()
        gen.writeName("0")
        gen.writeString(value.version)
        gen.writeName("1")
        provider.writeValue(gen, value.security)
        gen.writeName("2")
        gen.writeStartArray()
        value.deviceRetrievalMethods.forEach {
            provider.writeValue(gen, it)
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }
}

object DeviceEngagementCbor {
    val mapper: CBORMapper = CBORMapper.builder(CBORFactory())
        .addModule(KotlinModule.Builder().build())
        .addModule(
            SimpleModule().apply {
                addSerializer(DeviceEngagement::class.java, DeviceEngagementSerializer())
                addSerializer(BleOptions::class.java, BleOptionsSerializer())
                addSerializer(Security::class.java, SecuritySerializer())
                addSerializer(EmbeddedCbor::class.java, EmbeddedCborSerializer())
                addSerializer(
                    DeviceRetrievalMethod::class.java,
                    DeviceRetrievalMethodSerializer()
                )
            }
        )
        .build()

    fun encode(deviceEngagement: DeviceEngagement): ByteArray =
        mapper.writeValueAsBytes(deviceEngagement)
}
