package uk.gov.onelogin.sharing.cryptoService.cbor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.RawCborSerializer

/**
 * A factory object for creating and configuring the shared Jackson [ObjectMapper]
 * specifically for CBOR serialization.
 */
object CborMapper {
    val default: ObjectMapper = CBORMapper.builder(CBORFactory())
        .addModule(KotlinModule.Builder().build())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .addModule(
            SimpleModule().apply {
                addSerializer(EmbeddedCborSerializer())
                addSerializer(RawCborSerializer())
            }
        ).build()
}
