package uk.gov.onelogin.sharing.models.mdoc.engagment

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.module.SimpleModule
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

object CborMappers {
    fun default(): ObjectMapper = CBORMapper.builder(CBORFactory())
        .addModule(KotlinModule.Builder().build())
        .addModule(
            SimpleModule().apply {
                addSerializer(DeviceEngagement::class.java, DeviceEngagementSerializer())
                addSerializer(DeviceRetrievalMethod::class.java, DeviceRetrievalMethodSerializer())
                addSerializer(BleOptions::class.java, BleOptionsSerializer())
                addSerializer(Security::class.java, SecuritySerializer())
                addSerializer(EmbeddedCbor::class.java, EmbeddedCborSerializer())
            }
        )
        .build()
}
