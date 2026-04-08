package uk.gov.onelogin.sharing.cryptoService.cbor

import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceResponseDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSignedItem

/**
 * Maps the [DeviceResponse] domain model to its corresponding [DeviceResponseDto.DeviceResponse].
 */
fun DeviceResponse.toDto(): DeviceResponseDto.DeviceResponse = DeviceResponseDto.DeviceResponse(
    version = version,
    documents = documents?.map { it.toDto() },
    documentErrors = documentErrors?.mapValues { it.value.code },
    status = status.code
)

/**
 * Maps the [Document] domain model to its corresponding [DeviceResponseDto.DocumentDTO].
 */
fun Document.toDto(): DeviceResponseDto.DocumentDTO = DeviceResponseDto.DocumentDTO(
    docType = docType,
    issuerSigned = issuerSigned.toDto(),
    deviceSigned = deviceSigned.toDto(),
    errors = null
)

/**
 * Maps [IssuerSigned] domain model to its corresponding [DeviceResponseDto.IssuerSignedDTO].
 */
fun IssuerSigned.toDto(): DeviceResponseDto.IssuerSignedDTO = DeviceResponseDto.IssuerSignedDTO(
    nameSpaces = nameSpaces?.mapValues { entry ->
        entry.value.map { it.toEmbeddedCbor() }
    },
    issuerAuth = issuerAuth
)

/**
 * Encodes [IssuerSignedItem] into [EmbeddedCbor] (wrapped in Tag 24).
 */
fun IssuerSignedItem.toEmbeddedCbor(): EmbeddedCbor {
    val mapper = CborMapper.create(emptyMap())
    val dto = DeviceResponseDto.IssuerSignedItemDTO(
        digestId = digestId,
        random = random,
        elementIdentifier = elementIdentifier,
        elementValue = elementValue
    )
    return EmbeddedCbor(mapper.writeValueAsBytes(dto))
}

/**
 * Maps [DeviceSigned] domain model to its corresponding [DeviceResponseDto.DeviceSignedDTO].
 */
fun DeviceSigned.toDto(): DeviceResponseDto.DeviceSignedDTO = DeviceResponseDto.DeviceSignedDTO(
    nameSpaces = EmbeddedCbor(nameSpaces),
    deviceAuth = DeviceResponseDto.DeviceAuthDTO(
        deviceSignature = deviceSignature
    )
)
