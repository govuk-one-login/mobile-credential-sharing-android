package uk.gov.onelogin.sharing.security.cbor.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import uk.gov.onelogin.sharing.security.cbor.dto.BleOptionsDto
import uk.gov.onelogin.sharing.security.cbor.dto.DeviceRetrievalMethodDto

class DeviceRetrievalMethodsDeserializer : JsonDeserializer<List<DeviceRetrievalMethodDto>>() {

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): List<DeviceRetrievalMethodDto> {
        val root = p.codec.readTree<JsonNode>(p)

        return root.map { item ->
            val type = item[0].intValue()
            val version = item[1].intValue()

            val optsNode = item[2]

            val options = BleOptionsDto(
                serverMode = optsNode["0"].booleanValue(),
                clientMode = optsNode["1"].booleanValue(),
                peripheralServerModeUuid =
                    if (optsNode.has("10")) optsNode["10"].binaryValue() else null
            )

            DeviceRetrievalMethodDto(type, version, options)
        }
    }
}
