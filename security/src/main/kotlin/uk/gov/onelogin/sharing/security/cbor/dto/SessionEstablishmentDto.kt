package uk.gov.onelogin.sharing.security.cbor.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import uk.gov.onelogin.sharing.core.implementation.ImplementationDetail
import uk.gov.onelogin.sharing.core.implementation.RequiresImplementation
import uk.gov.onelogin.sharing.security.cbor.deserializers.SessionEstablishmentDeserializer
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor

@RequiresImplementation(
    details = [
        ImplementationDetail(
            ticket = "DCMAW-16759",
            description = "Build DeviceRequest and nested models"
        )
    ]
)
@JsonDeserialize(using = SessionEstablishmentDeserializer::class)
data class SessionEstablishmentDto(
    @JsonProperty("eReaderKey")
    val eReaderKey: EmbeddedCbor,
    @JsonProperty("data")
    val data: ByteArray
)
