package uk.gov.onelogin.sharing.security.cbor

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer
import tools.jackson.dataformat.cbor.CBORFactory
import tools.jackson.dataformat.cbor.CBORMapper
import tools.jackson.module.kotlin.KotlinModule

/**
 * A factory object for creating and configuring Jackson [ObjectMapper] instances
 * specifically for CBOR serialization.
 *
 * This object centralizes the setup of [CBORMapper] and provides a way to add custom serializers.
 */
object CborMapper {

    /**
     * Creates a configured [ObjectMapper] for CBOR serialization with a given
     * set of custom serializers.
     *
     * This method builds a [CBORMapper], registers the necessary [KotlinModule] for
     * handling Kotlin-specific types, and then registers a [SimpleModule] containing
     * all the custom serializers provided by the caller. This is the primary entry point
     * for creating a mapper capable of serializing custom data structures into CBOR.
     *
     * @param serializers A map where the key is the [Class] to be serialized and the
     *                    value is the [StdSerializer] responsible for its conversion
     *                    to CBOR.
     * @return A fully configured [ObjectMapper] ready for CBOR serialization.
     */
    fun create(serializers: Map<Class<*>, StdSerializer<*>>): ObjectMapper =
        CBORMapper.builder(CBORFactory())
            .addModule(KotlinModule.Builder().build())
            .addModule(
                SimpleModule().apply {
                    serializers.forEach { (clazz, serializer) ->
                        @Suppress("UNCHECKED_CAST")
                        addSerializer(clazz as Class<Any>, serializer as StdSerializer<Any>)
                    }
                }
            ).build()
}
