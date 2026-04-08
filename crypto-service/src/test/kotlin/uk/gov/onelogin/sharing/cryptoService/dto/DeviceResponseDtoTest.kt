package uk.gov.onelogin.sharing.cryptoService.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceResponseDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer

class DeviceResponseDtoTest {

    private val mapper = CborMapper.create(
        mapOf(EmbeddedCbor::class.java to EmbeddedCborSerializer())
    )

    private val docType = "org.iso.18013.5.1.mDL"

    @Test
    fun `Validate CBOR Tag 24 for IssuerSigned and DeviceSigned`() {
        val issuerSignedItemData = byteArrayOf(0x01, 0x02)
        val deviceNameSpacesData = byteArrayOf(0x03, 0x04)

        val document = DeviceResponseDto.DocumentDTO(
            docType = docType,
            issuerSigned = DeviceResponseDto.IssuerSignedDTO(
                nameSpaces = mapOf(
                    "org.iso.18013.5.1" to listOf(EmbeddedCbor(issuerSignedItemData))
                ),
                issuerAuth = byteArrayOf()
            ),
            deviceSigned = DeviceResponseDto.DeviceSignedDTO(
                nameSpaces = EmbeddedCbor(deviceNameSpacesData),
                deviceAuth = DeviceResponseDto.DeviceAuthDTO(
                    deviceSignature = byteArrayOf()
                )
            )
        )

        val deviceResponse = DeviceResponseDto.DeviceResponse(
            documents = listOf(document),
            documentErrors = null,
            status = 0
        )

        val encoded = mapper.writeValueAsBytes(deviceResponse)

        val tag24 = byteArrayOf(0xd8.toByte(), 24.toByte())
        val tag24Hex = tag24.joinToString("") { "%02x".format(it) }
        val cborHeader = "42"

        val issuerSignedItemHex = issuerSignedItemData.joinToString("")
            { "%02x".format(it) }
        val deviceNameSpacesHex = deviceNameSpacesData.joinToString("")
            { "%02x".format(it) }

        val encodedString = encoded.joinToString("") { "%02x".format(it) }

        assertTrue(
            "Encoded output should contain tagged issuerSigned item data",
            encodedString.contains("$tag24Hex$cborHeader$issuerSignedItemHex")
        )
        assertTrue(
            "Encoded output should contain tagged deviceSigned namespaces data",
            encodedString.contains("$tag24Hex$cborHeader$deviceNameSpacesHex")
        )
    }

    @Test
    fun `Instantiate DeviceResponse model for user denial scenario`() {
        val deviceResponse = DeviceResponseDto.DeviceResponse(
            status = 0,
            documents = null,
            documentErrors = mapOf(docType to 0)
        )

        assertEquals(0, deviceResponse.status)

        assertNull(deviceResponse.documents)

        assertEquals(1, deviceResponse.documentErrors?.size)
        assertEquals(0, deviceResponse.documentErrors?.get(docType))
    }
}
