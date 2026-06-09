package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BinaryNode
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable

data class ItemsRequestDto(
    @JsonProperty("docType")
    val docType: String,
    @JsonProperty("nameSpaces")
    val nameSpaces: Map<String, Map<String, Boolean>>,
    @JsonIgnore
    val requestInfo: ByteArray? = null
) : CborEncodable {
    init {
        require(docType.isNotEmpty()) { "ItemsRequest: docType must not be empty" }
        require(nameSpaces.isNotEmpty()) { "ItemsRequest: nameSpaces must not be empty" }
    }

    fun toDomain(): ItemsRequest = ItemsRequest(docType = docType, nameSpaces = nameSpaces)

    /**
     * Unwraps Tag 24-encoded bstr and re-deserializes the inner bytes as [ItemsRequestDto].
     */
    class Deserializer : JsonDeserializer<ItemsRequestDto>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ItemsRequestDto {
            val binaryNode = p.codec.readTree<JsonNode>(p) as BinaryNode
            return (p.codec as ObjectMapper).readValue(
                binaryNode.binaryValue(),
                ItemsRequestDto::class.java
            )
        }
    }
}

fun ItemsRequest.toDto(): ItemsRequestDto = ItemsRequestDto(
    docType = docType,
    nameSpaces = nameSpaces
)
