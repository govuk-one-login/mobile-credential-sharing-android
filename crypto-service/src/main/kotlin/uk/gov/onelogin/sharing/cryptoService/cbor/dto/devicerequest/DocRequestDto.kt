package uk.gov.onelogin.sharing.cryptoService.cbor.dto.devicerequest

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uk.gov.onelogin.sharing.cryptoService.cbor.deserializers.ItemsRequestDeserializer

data class DocRequestDto(
    @JsonProperty("itemsRequest")
    @JsonDeserialize(using = ItemsRequestDeserializer::class)
    val itemsRequest: ItemsRequestDto,
    @JsonIgnore
    val readerAuth: ByteArray? = null
)
