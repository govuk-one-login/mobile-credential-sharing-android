package uk.gov.onelogin.sharing.cryptoService.dto

import com.fasterxml.jackson.dataformat.cbor.CBORConstants.PREFIX_TYPE_BYTES
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.io.ByteArrayOutputStream
import kotlin.test.assertContains
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.DeviceResponseDto
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.RawCbor
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

@RunWith(TestParameterInjector::class)
class DeviceResponseDtoTest {

    private val mapper = CborMapper.default

    private val docType = "org.iso.18013.5.1.mDL"
    private val hexFormatter: (Any) -> CharSequence = { "%02x".format(it) }
    private val deviceNameSpacesData = ByteArrayOutputStream().also { out ->
        CBORFactory().createGenerator(out).use { gen ->
            gen.writeStartObject(0)
            gen.writeEndObject()
        }
    }.toByteArray()
    private val validDeviceSignedDto = DeviceResponseDto.DeviceSignedDTO(
        nameSpaces = EmbeddedCbor(deviceNameSpacesData),
        deviceAuth = DeviceResponseDto.DeviceAuthDTO(
            deviceSignature = RawCbor(byteArrayOf())
        )
    )

    private val tag24 = byteArrayOf(0xd8.toByte(), 24.toByte())
    private val tag24Hex = tag24.joinToString("", transform = hexFormatter)

    @Test
    fun `Validate CBOR Tag 24 for IssuerSigned and DeviceSigned`() {
        val issuerSignedItemData = byteArrayOf(0x01, 0x02)

        val document = DeviceResponseDto.DocumentDTO(
            docType = docType,
            issuerSigned = DeviceResponseDto.IssuerSignedDTO(
                nameSpaces = mapOf(
                    "org.iso.18013.5.1" to listOf(EmbeddedCbor(issuerSignedItemData))
                ),
                issuerAuth = RawCbor(byteArrayOf())
            ),
            deviceSigned = validDeviceSignedDto
        )

        val deviceResponse = DeviceResponseDto.DeviceResponse(
            documents = listOf(document),
            status = 0u
        )

        val encoded = mapper.writeValueAsBytes(deviceResponse)

        val issuerSignedItemHex = issuerSignedItemData.joinToString("", transform = hexFormatter)
        val deviceNameSpacesHex = deviceNameSpacesData.joinToString("", transform = hexFormatter)

        val encodedString = encoded.joinToString("", transform = hexFormatter)

        assertContains(
            message = "Encoded output should contain tagged issuerSigned item data",
            charSequence = encodedString,
            other = "$tag24Hex${generateBytesTag(2)}$issuerSignedItemHex"
        )

        assertContains(
            message = "Encoded output should contain tagged deviceSigned namespaces data",
            charSequence = encodedString,
            other = "$tag24Hex${generateBytesTag(1)}$deviceNameSpacesHex"
        )
    }

    /**
     * DCMAW-19837: AC4: Enforce empty namespaces for DeviceSigned items
     */
    @Test
    fun `Validate CBOR structure for DeviceSignedDto nameSpaces`() {
        val deviceNameSpacesHex = deviceNameSpacesData.joinToString("", transform = hexFormatter)

        val encoded = mapper.writeValueAsBytes(validDeviceSignedDto)
        val encodedString = encoded.joinToString("", transform = hexFormatter)

        assertContains(
            message = "Encoded output should contain tagged deviceSigned namespaces data",
            charSequence = encodedString,
            other = "$tag24Hex${generateBytesTag(1)}$deviceNameSpacesHex"
        )
    }

    /**
     * DCMAW-19837: AC4: Enforce empty namespaces for DeviceSigned items
     */
    @Test
    fun `DeviceSignedDto instances with namespace data throw IllegalArgumentExceptions`() {
        val nameSpacesData = mapOf(
            "portrait" to false,
            "age_over_21" to false
        )

        val nameSpacesBytes = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(nameSpacesData.size)
                nameSpacesData.forEach { (key, value) ->
                    gen.writeBooleanField(key, value)
                }
                gen.writeEndObject()
            }
        }.toByteArray()

        val exception = assertThrows(IllegalArgumentException::class.java) {
            DeviceResponseDto.DeviceSignedDTO(
                nameSpaces = EmbeddedCbor(nameSpacesBytes),
                deviceAuth = DeviceResponseDto.DeviceAuthDTO(
                    deviceSignature = RawCbor(byteArrayOf())
                )
            )
        }

        assertThat(
            exception.message,
            containsString(
                "Received unexpected data in 'nameSpaces' property: $nameSpacesData"
            )
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

    private fun generateBytesTag(elementSize: Int) = hexFormatter(PREFIX_TYPE_BYTES + elementSize)
}
