package uk.gov.onelogin.sharing.security.cbor.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BinaryNode
import uk.gov.onelogin.sharing.security.cbor.dto.devicerequest.ItemsRequestDto

class ItemsRequestDeserializer : JsonDeserializer<ItemsRequestDto>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ItemsRequestDto {
        val root = p.codec.readTree<JsonNode>(p)
        val binaryNode = root as BinaryNode
        val byteArray = binaryNode.binaryValue()

        val cborMapper = p.codec as ObjectMapper

        return cborMapper.readValue(
            byteArray,
            ItemsRequestDto::class.java
        )
    }
}
