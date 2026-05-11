package uk.gov.onelogin.sharing.cryptoService.dto

import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceResponseDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.RawCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.RawCborSerializer
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

@RunWith(TestParameterInjector::class)
class DeviceResponseDtoTest {

    private val mapper = CborMapper.create(
        mapOf(
            EmbeddedCbor::class.java to EmbeddedCborSerializer(),
            RawCbor::class.java to RawCborSerializer()
        )
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
                issuerAuth = RawCbor(byteArrayOf())
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
            status = 0u
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
            status = 0u,
            documentErrors = mapOf(docType to 0u)
        )

        assertEquals(0u, deviceResponse.status)

        assertNull(deviceResponse.documents)

        assertEquals(1, deviceResponse.documentErrors?.size)
        assertEquals(0u, deviceResponse.documentErrors?.get(docType))
    }

    /**
     * DCMAW-19837: AC2: Enforce DeviceResponse version constraints
     */
    @Test
    fun `Valid device responses have '1' as the major version`(
        @TestParameter version: String = testValues(
            "1.0",
            "1.x"
        )
    ) {
        DeviceResponseDto.DeviceResponse(
            version = version,
            status = 0u
        )
    }

    /**
     * DCMAW-19837: AC2: Enforce DeviceResponse version constraints
     */
    @Test
    fun `Invalid versions throw IllegalArgumentExceptions`(
        @TestParameter version: String = testValues(
            "2.0",
            "0.0"
        )
    ) {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            DeviceResponseDto.DeviceResponse(
                version = version,
                status = 0u
            )
        }

        assertThat(
            exception.message,
            equalTo("Received invalid device response version: $version")
        )
    }

    /**
     * DCMAW-19837: AC3: Enforce DeviceResponse status constraints
     */
    @Test
    fun `Valid status codes come from the 'Status' enum`(@TestParameter status: Status) {
        DeviceResponseDto.DeviceResponse(status = status.code)
    }

    /**
     * DCMAW-19837: AC3: Enforce DeviceResponse status constraints
     */
    @Test
    fun `Invalid status codes throw IllegalArgumentExceptions`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            DeviceResponseDto.DeviceResponse(status = 13u)
        }

        assertThat(
            exception.message,
            equalTo("Received invalid device response status code: 13")
        )
    }
}
