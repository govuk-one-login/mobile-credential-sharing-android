package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor

class DeviceResponseCborExtractorTest {

    private val mapper = CborMapper.default

    @Test
    fun `extracts issuerSignedItemBytes preserving Tag 24 envelope`() {
        val itemData = byteArrayOf(0xA4.toByte(), 0x01, 0x02, 0x03, 0x04)
        val dto = DeviceResponseDtoStub.deviceResponseDto(
            nameSpaces = mapOf(
                "org.iso.18013.5.1" to listOf(EmbeddedCbor(itemData))
            )
        )

        val encoded = mapper.writeValueAsBytes(dto)
        val result = DeviceResponseCborExtractor.extract(encoded)

        val items = result[0].issuerSigned.nameSpaces["org.iso.18013.5.1"]!!
        assertThat(items.size, equalTo(1))

        // Extracted bytes must include the Tag 24 (0xD8 0x18) prefix
        assertThat(items[0][0], equalTo(0xD8.toByte()))
        assertThat(items[0][1], equalTo(0x18.toByte()))
    }

    @Test
    fun `extracts issuerAuthBytes as raw CBOR`() {
        val coseSign1 = DeviceResponseDtoStub.coseSign1WithIntegerKeys
        val dto = DeviceResponseDtoStub.deviceResponseDto(issuerAuth = coseSign1)

        val encoded = mapper.writeValueAsBytes(dto)
        val result = DeviceResponseCborExtractor.extract(encoded)

        assertThat(result[0].issuerSigned.issuerAuthBytes, equalTo(coseSign1))
    }

    @Test
    fun `extracts deviceSignatureBytes as raw CBOR`() {
        val coseSign1 = DeviceResponseDtoStub.coseSign1WithIntegerKeys
        val dto = DeviceResponseDtoStub.deviceResponseDto(deviceSignature = coseSign1)

        val encoded = mapper.writeValueAsBytes(dto)
        val result = DeviceResponseCborExtractor.extract(encoded)

        assertThat(result[0].deviceSigned.signatureBytes, equalTo(coseSign1))
    }

    @Test
    fun `extracts deviceNameSpacesBytes preserving Tag 24 envelope`() {
        val dto = DeviceResponseDtoStub.deviceResponseDto()

        val encoded = mapper.writeValueAsBytes(dto)
        val result = DeviceResponseCborExtractor.extract(encoded)

        val nameSpacesBytes = result[0].deviceSigned.nameSpacesBytes!!
        // Must include the Tag 24 (0xD8 0x18) prefix
        assertThat(nameSpacesBytes[0], equalTo(0xD8.toByte()))
        assertThat(nameSpacesBytes[1], equalTo(0x18.toByte()))
    }

    @Test
    fun `returns empty list when no documents present`() {
        val dto = DeviceResponseDto.DeviceResponseDTO(status = 0u)

        val encoded = mapper.writeValueAsBytes(dto)
        val result = DeviceResponseCborExtractor.extract(encoded)

        assertThat(result.size, equalTo(0))
    }

    @Test
    fun `extracts raw bytes for multiple documents`() {
        val coseSign1 = DeviceResponseDtoStub.coseSign1WithIntegerKeys
        val dto = DeviceResponseDto.DeviceResponseDTO(
            version = "1.0",
            documents = listOf(
                DeviceResponseDtoStub.documentDto(issuerAuth = coseSign1),
                DeviceResponseDtoStub.documentDto(deviceSignature = coseSign1)
            ),
            status = 0u
        )

        val encoded = mapper.writeValueAsBytes(dto)
        val result = DeviceResponseCborExtractor.extract(encoded)

        assertThat(result.size, equalTo(2))
        assertThat(result[0].issuerSigned.issuerAuthBytes, equalTo(coseSign1))
        assertThat(result[1].deviceSigned.signatureBytes, equalTo(coseSign1))
    }

    @Test
    fun `returns empty nameSpaces when issuerSigned has no nameSpaces`() {
        val dto = DeviceResponseDtoStub.deviceResponseDto(nameSpaces = null)

        val encoded = mapper.writeValueAsBytes(dto)
        val result = DeviceResponseCborExtractor.extract(encoded)

        assertThat(result[0].issuerSigned.nameSpaces, equalTo(emptyMap()))
    }

    @Test
    fun `extracts multiple namespaces with multiple items`() {
        val item1 = byteArrayOf(0x01, 0x02)
        val item2 = byteArrayOf(0x03, 0x04)
        val item3 = byteArrayOf(0x05, 0x06)

        val dto = DeviceResponseDtoStub.deviceResponseDto(
            nameSpaces = mapOf(
                "org.iso.18013.5.1" to listOf(EmbeddedCbor(item1), EmbeddedCbor(item2)),
                "org.iso.18013.5.1.aamva" to listOf(EmbeddedCbor(item3))
            )
        )

        val encoded = mapper.writeValueAsBytes(dto)
        val result = DeviceResponseCborExtractor.extract(encoded)

        val nameSpaces = result[0].issuerSigned.nameSpaces
        assertThat(nameSpaces["org.iso.18013.5.1"]!!.size, equalTo(2))
        assertThat(nameSpaces["org.iso.18013.5.1.aamva"]!!.size, equalTo(1))
    }
}
