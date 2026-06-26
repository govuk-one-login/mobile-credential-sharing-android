package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborEncodable

@JsonDeserialize(using = ItemsRequestDto.Deserializer::class)
data class ItemsRequestDto(
    val docType: String,
    val nameSpaces: Map<String, Map<String, Boolean>>,
    @JsonIgnore
    val requestInfo: ByteArray? = null
) : CborEncodable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemsRequestDto

        if (docType != other.docType) return false
        if (nameSpaces != other.nameSpaces) return false
        if (!requestInfo.contentEquals(other.requestInfo)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = docType.hashCode()
        result = 31 * result + nameSpaces.hashCode()
        result = 31 * result + (requestInfo?.contentHashCode() ?: 0)
        return result
    }

    init {
        require(docType.isNotEmpty()) { "ItemsRequest: docType must not be empty" }
        require(nameSpaces.isNotEmpty()) { "ItemsRequest: nameSpaces must not be empty" }
    }

    fun toDomain(): ItemsRequest = ItemsRequest(docType = docType, nameSpaces = nameSpaces)

    /**
     * Deserializes [ItemsRequestDto] from a CBOR map with "docType" and "nameSpaces" fields.
     */
    class Deserializer : StdDeserializer<ItemsRequestDto>(ItemsRequestDto::class.java) {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ItemsRequestDto {
            val root = p.codec.readTree<JsonNode>(p)
            val docType = root["docType"]?.asText()
                ?: throw IllegalArgumentException("Missing docType in ItemsRequest")
            val nameSpacesNode = root["nameSpaces"]
                ?: throw IllegalArgumentException("Missing nameSpaces in ItemsRequest")
            val nameSpaces = mutableMapOf<String, Map<String, Boolean>>()
            nameSpacesNode.properties().forEach { (ns, elements) ->
                val elementMap = mutableMapOf<String, Boolean>()
                elements.properties().forEach { (key, value) ->
                    elementMap[key] = value.booleanValue()
                }
                nameSpaces[ns] = elementMap
            }
            return ItemsRequestDto(docType = docType, nameSpaces = nameSpaces)
        }
    }
}

fun ItemsRequest.toDto(): ItemsRequestDto = ItemsRequestDto(
    docType = docType,
    nameSpaces = nameSpaces
)
