package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.RawCbor

object DeviceResponseDtoStub {

    private val emptyMapBytes = ByteArrayOutputStream().also { out ->
        CBORFactory().createGenerator(out).use { gen ->
            gen.writeStartObject(0)
            gen.writeEndObject()
        }
    }.toByteArray()

    val issuerAuthBytes: ByteArray = CborMapper.default.writeValueAsBytes(listOf<Any>())

    val deviceSignedDto = DeviceResponseDto.DeviceSignedDTO(
        nameSpaces = EmbeddedCbor(emptyMapBytes),
        deviceAuth = DeviceResponseDto.DeviceAuthDTO(
            deviceSignature = RawCbor(CborMapper.default.writeValueAsBytes(listOf<Any>()))
        )
    )

    fun issuerSignedDto(nameSpaces: Map<String, List<EmbeddedCbor>>? = null) =
        DeviceResponseDto.IssuerSignedDTO(
            nameSpaces = nameSpaces,
            issuerAuth = RawCbor(issuerAuthBytes)
        )

    fun documentDto(
        docType: String = "org.iso.18013.5.1.mDL",
        nameSpaces: Map<String, List<EmbeddedCbor>>? = null
    ) = DeviceResponseDto.DocumentDTO(
        docType = docType,
        issuerSigned = issuerSignedDto(nameSpaces),
        deviceSigned = deviceSignedDto
    )

    fun deviceResponseDto(nameSpaces: Map<String, List<EmbeddedCbor>>? = null) =
        DeviceResponseDto.DeviceResponseDTO(
            version = "1.0",
            documents = listOf(documentDto(nameSpaces = nameSpaces)),
            status = 0u
        )
}
