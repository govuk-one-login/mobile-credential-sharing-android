package uk.gov.onelogin.sharing.cryptoService.cbor

import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceResponseDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.RawCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned

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
 * Each ByteArray in nameSpaces is the original Tag 24 encoded IssuerSignedItemBytes.
 */
fun IssuerSigned.toDto(): DeviceResponseDto.IssuerSignedDTO = DeviceResponseDto.IssuerSignedDTO(
    nameSpaces = nameSpaces?.mapValues { entry ->
        entry.value.map { EmbeddedCbor(it) }
    },
    issuerAuth = RawCbor(issuerAuth)
)

/**
 * Encodes [IssuerSigned] to CBOR bytes. For diagnostic use only.
 */
fun IssuerSigned.encodeCbor(): ByteArray = CborMapper.default
    .writeValueAsBytes(this.toDto())

/**
 * Maps [DeviceSigned] domain model to its corresponding [DeviceResponseDto.DeviceSignedDTO].
 */
fun DeviceSigned.toDto(): DeviceResponseDto.DeviceSignedDTO = DeviceResponseDto.DeviceSignedDTO(
    nameSpaces = EmbeddedCbor(nameSpaces),
    deviceAuth = DeviceResponseDto.DeviceAuthDTO(deviceSignature = RawCbor(deviceAuth))
)
