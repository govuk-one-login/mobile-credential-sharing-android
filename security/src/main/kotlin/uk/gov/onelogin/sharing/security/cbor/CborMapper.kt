package uk.gov.onelogin.sharing.security.cbor

import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.ser.std.StdSerializer
import tools.jackson.dataformat.cbor.CBORFactory
import tools.jackson.dataformat.cbor.CBORMapper
import tools.jackson.module.kotlin.KotlinModule

object CborMapper {
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
