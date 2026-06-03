package uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

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
}

fun ItemsRequest.toDto(): ItemsRequestDto = ItemsRequestDto(
    docType = docType,
    nameSpaces = nameSpaces
)
