package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.RawCbor

object DeviceResponseDtoStub {

    private val cborFactory = CBORFactory()

    private val emptyMapBytes = ByteArrayOutputStream().also { out ->
        cborFactory.createGenerator(out).use { gen ->
            gen.writeStartObject(0)
            gen.writeEndObject()
        }
    }.toByteArray()

    val issuerAuthBytes: ByteArray = CborMapper.default.writeValueAsBytes(listOf<Any>())

    /**
     * A COSE_Sign1 structure with integer map keys in the protected header ({1: -7}).
     * Useful for verifying that raw byte preservation avoids Jackson re-encoding
     * which would corrupt integer keys to string keys.
     */
    val coseSign1WithIntegerKeys: ByteArray = run {
        val protectedHeader = ByteArrayOutputStream().also { out ->
            cborFactory.createGenerator(out).use { gen ->
                gen.writeStartObject(1)
                gen.writeFieldId(1)
                gen.writeNumber(-7L)
                gen.writeEndObject()
            }
        }.toByteArray()

        ByteArrayOutputStream().also { out ->
            cborFactory.createGenerator(out).use { gen ->
                gen.writeStartArray(null, 4)
                gen.writeBinary(protectedHeader)
                gen.writeStartObject(0)
                gen.writeEndObject()
                gen.writeNull()
                gen.writeBinary(byteArrayOf(0x01, 0x02, 0x03))
                gen.writeEndArray()
            }
        }.toByteArray()
    }

    fun issuerSignedDto(
        nameSpaces: Map<String, List<EmbeddedCbor>>? = null,
        issuerAuth: ByteArray = issuerAuthBytes
    ) = DeviceResponseDto.IssuerSignedDTO(
        nameSpaces = nameSpaces,
        issuerAuth = RawCbor(issuerAuth)
    )

    fun deviceSignedDto(
        deviceSignature: ByteArray = CborMapper.default.writeValueAsBytes(listOf<Any>())
    ) = DeviceResponseDto.DeviceSignedDTO(
        nameSpaces = EmbeddedCbor(emptyMapBytes),
        deviceAuth = DeviceResponseDto.DeviceAuthDTO(
            deviceSignature = RawCbor(deviceSignature)
        )
    )

    fun documentDto(
        docType: String = "org.iso.18013.5.1.mDL",
        nameSpaces: Map<String, List<EmbeddedCbor>>? = null,
        issuerAuth: ByteArray = issuerAuthBytes,
        deviceSignature: ByteArray = CborMapper.default.writeValueAsBytes(listOf<Any>())
    ) = DeviceResponseDto.DocumentDTO(
        docType = docType,
        issuerSigned = issuerSignedDto(nameSpaces, issuerAuth),
        deviceSigned = deviceSignedDto(deviceSignature)
    )

    fun deviceResponseDto(
        nameSpaces: Map<String, List<EmbeddedCbor>>? = null,
        issuerAuth: ByteArray = issuerAuthBytes,
        deviceSignature: ByteArray = CborMapper.default.writeValueAsBytes(listOf<Any>())
    ) = DeviceResponseDto.DeviceResponseDTO(
        version = "1.0",
        documents = listOf(
            documentDto(
                nameSpaces = nameSpaces,
                issuerAuth = issuerAuth,
                deviceSignature = deviceSignature
            )
        ),
        status = 0u
    )
}
