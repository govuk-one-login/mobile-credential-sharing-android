package uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.onelogin.sharing.cryptoService.cbor.CborEncodable
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

data class DeviceRequestDto(
    @JsonProperty("version")
    val version: String,
    @JsonProperty("docRequests")
    val docRequest: List<DocRequestDto>,
    @JsonIgnore
    val deviceRequestInfo: ByteArray? = null,
    @JsonIgnore
    val readerAuthAll: ByteArray? = null
) : CborEncodable {
    init {
        require(version.isNotEmpty()) { "DeviceRequest: version must not be empty" }
        require(docRequest.isNotEmpty()) { "DeviceRequest: docRequests must not be empty" }
    }

    fun toDomain(): DeviceRequest = DeviceRequest(
        version = version,
        docRequests = docRequest.map {
            DocRequest(ItemsRequest(it.itemsRequest.docType, it.itemsRequest.nameSpaces))
        }
    )
}

fun DeviceRequest.toDto(): DeviceRequestDto = DeviceRequestDto(
    version = version,
    docRequest = docRequests.map { it.toDto() }
)
