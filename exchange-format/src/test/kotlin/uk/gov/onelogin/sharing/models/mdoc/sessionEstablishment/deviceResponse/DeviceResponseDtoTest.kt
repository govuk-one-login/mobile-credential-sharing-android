package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse

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
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.cbor.HexFormatter
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.serializers.RawCbor

@RunWith(TestParameterInjector::class)
class DeviceResponseDtoTest {

    private val mapper = CborMapper.default

    private val docType = "org.iso.18013.5.1.mDL"
    private val hexFormatter: (Any) -> CharSequence = HexFormatter::invoke
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

        val deviceResponse = DeviceResponseDto.DeviceResponseDTO(
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
        val deviceResponse = DeviceResponseDto.DeviceResponseDTO(
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
        DeviceResponseDto.DeviceResponseDTO(
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
            DeviceResponseDto.DeviceResponseDTO(
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
        DeviceResponseDto.DeviceResponseDTO(status = status.code)
    }

    /**
     * DCMAW-19837: AC3: Enforce DeviceResponse status constraints
     */
    @Test
    fun `Invalid status codes throw IllegalArgumentExceptions`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            DeviceResponseDto.DeviceResponseDTO(status = 13u)
        }

        assertThat(
            exception.message,
            equalTo("Received invalid device response status code: 13")
        )
    }

    @Test
    fun `toDomain maps DeviceResponseDTO to DeviceResponse`() {
        val issuerSignedItemData = byteArrayOf(0x01, 0x02)
        val issuerAuthBytes = mapper.writeValueAsBytes(listOf<Any>())
        val deviceSignatureData = mapper.writeValueAsBytes(listOf<Any>())

        val dto = DeviceResponseDto.DeviceResponseDTO(
            version = "1.0",
            documents = listOf(
                DeviceResponseDto.DocumentDTO(
                    docType = docType,
                    issuerSigned = DeviceResponseDto.IssuerSignedDTO(
                        nameSpaces = mapOf(
                            "org.iso.18013.5.1" to listOf(EmbeddedCbor(issuerSignedItemData))
                        ),
                        issuerAuth = RawCbor(issuerAuthBytes)
                    ),
                    deviceSigned = DeviceResponseDto.DeviceSignedDTO(
                        nameSpaces = EmbeddedCbor(deviceNameSpacesData),
                        deviceAuth = DeviceResponseDto.DeviceAuthDTO(
                            deviceSignature = RawCbor(deviceSignatureData)
                        )
                    )
                )
            ),
            status = 0u
        )

        val encoded = mapper.writeValueAsBytes(dto)
        val decoded = mapper.readValue(encoded, DeviceResponseDto.DeviceResponseDTO::class.java)
        val domain = decoded.toDomain(encoded)

        assertEquals("1.0", domain.version)
        assertEquals(Status.OK, domain.status)

        val documents = domain.documents!!
        assertEquals(1, documents.size)
        assertEquals(docType, documents.first().docType)

        val issuerSigned = documents.first().issuerSigned
        val nameSpaces = issuerSigned.nameSpaces!!
        assertEquals(1, nameSpaces["org.iso.18013.5.1"]!!.size)
        assertThat(nameSpaces["org.iso.18013.5.1"]!![0], equalTo(wrapTag24(issuerSignedItemData)))
        assertThat(issuerSigned.issuerAuth, equalTo(issuerAuthBytes))

        val deviceSigned = documents.first().deviceSigned
        assertThat(deviceSigned.deviceNameSpacesBytes, equalTo(wrapTag24(deviceNameSpacesData)))
        assertThat(deviceSigned.deviceSignature, equalTo(deviceSignatureData))
    }

    @Test
    fun `toDomain maps null documents`() {
        val dto = DeviceResponseDto.DeviceResponseDTO(status = 10u)

        val encoded = mapper.writeValueAsBytes(dto)
        val decoded = mapper.readValue(encoded, DeviceResponseDto.DeviceResponseDTO::class.java)
        val domain = decoded.toDomain(encoded)

        assertEquals(Status.GENERAL_ERROR, domain.status)
        assertNull(domain.documents)
    }

    @Test
    fun `serialisation and deserialisation preserves DeviceResponseDTO`() {
        val issuerSignedItemData = byteArrayOf(0x01, 0x02)

        // issuerAuth and deviceSignature must be valid CBOR for deserialisation
        val issuerAuthBytes = mapper.writeValueAsBytes(listOf<Any>())
        val deviceSignatureBytes = mapper.writeValueAsBytes(listOf<Any>())

        val original = DeviceResponseDto.DeviceResponseDTO(
            version = "1.0",
            documents = listOf(
                DeviceResponseDto.DocumentDTO(
                    docType = docType,
                    issuerSigned = DeviceResponseDto.IssuerSignedDTO(
                        nameSpaces = mapOf(
                            "org.iso.18013.5.1" to listOf(EmbeddedCbor(issuerSignedItemData))
                        ),
                        issuerAuth = RawCbor(issuerAuthBytes)
                    ),
                    deviceSigned = DeviceResponseDto.DeviceSignedDTO(
                        nameSpaces = EmbeddedCbor(deviceNameSpacesData),
                        deviceAuth = DeviceResponseDto.DeviceAuthDTO(
                            deviceSignature = RawCbor(deviceSignatureBytes)
                        )
                    )
                )
            ),
            status = 0u
        )

        val encoded = mapper.writeValueAsBytes(original)
        val decoded = mapper.readValue(
            encoded,
            DeviceResponseDto.DeviceResponseDTO::class.java
        )

        assertEquals(original.version, decoded.version)
        assertEquals(original.status, decoded.status)

        val originalDocs = original.documents!!
        val decodedDocs = decoded.documents!!
        assertEquals(originalDocs.size, decodedDocs.size)
        assertEquals(originalDocs[0].docType, decodedDocs[0].docType)
        assertThat(
            decodedDocs[0].issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![0].encoded,
            equalTo(issuerSignedItemData)
        )
        assertThat(
            decodedDocs[0].deviceSigned.nameSpaces.encoded,
            equalTo(deviceNameSpacesData)
        )
    }

    @Test
    fun `round trip with null documents omits documents field`() {
        val original = DeviceResponseDto.DeviceResponseDTO(
            status = 0u,
            documents = null
        )

        val encoded = mapper.writeValueAsBytes(original)
        val decoded = mapper.readValue(
            encoded,
            DeviceResponseDto.DeviceResponseDTO::class.java
        )

        assertEquals(original.version, decoded.version)
        assertEquals(original.status, decoded.status)
        assertNull(decoded.documents)
    }

    @Test
    fun `round trip with documentErrors preserves error map`() {
        val original = DeviceResponseDto.DeviceResponseDTO(
            status = 0u,
            documentErrors = mapOf(docType to 10u)
        )

        val encoded = mapper.writeValueAsBytes(original)
        val decoded = mapper.readTree(encoded)

        assertEquals(10, decoded["documentErrors"][docType].asInt())
    }

    @Test
    fun `round trip with null issuerSigned nameSpaces omits nameSpaces field`() {
        val issuerAuthBytes = mapper.writeValueAsBytes(listOf<Any>())
        val deviceSignatureBytes = mapper.writeValueAsBytes(listOf<Any>())

        val original = DeviceResponseDto.DeviceResponseDTO(
            documents = listOf(
                DeviceResponseDto.DocumentDTO(
                    docType = docType,
                    issuerSigned = DeviceResponseDto.IssuerSignedDTO(
                        nameSpaces = null,
                        issuerAuth = RawCbor(issuerAuthBytes)
                    ),
                    deviceSigned = DeviceResponseDto.DeviceSignedDTO(
                        nameSpaces = EmbeddedCbor(deviceNameSpacesData),
                        deviceAuth = DeviceResponseDto.DeviceAuthDTO(
                            deviceSignature = RawCbor(deviceSignatureBytes)
                        )
                    )
                )
            ),
            status = 0u
        )

        val encoded = mapper.writeValueAsBytes(original)
        val decoded = mapper.readValue(
            encoded,
            DeviceResponseDto.DeviceResponseDTO::class.java
        )

        val docs = decoded.documents!!
        assertNull(docs[0].issuerSigned.nameSpaces)
        assertEquals(docType, docs[0].docType)
    }

    @Test
    fun `round trip with DocumentDTO errors preserves errors map`() {
        val issuerAuthBytes = mapper.writeValueAsBytes(listOf<Any>())
        val deviceSignatureBytes = mapper.writeValueAsBytes(listOf<Any>())

        val original = DeviceResponseDto.DeviceResponseDTO(
            documents = listOf(
                DeviceResponseDto.DocumentDTO(
                    docType = docType,
                    issuerSigned = DeviceResponseDto.IssuerSignedDTO(
                        nameSpaces = null,
                        issuerAuth = RawCbor(issuerAuthBytes)
                    ),
                    deviceSigned = DeviceResponseDto.DeviceSignedDTO(
                        nameSpaces = EmbeddedCbor(deviceNameSpacesData),
                        deviceAuth = DeviceResponseDto.DeviceAuthDTO(
                            deviceSignature = RawCbor(deviceSignatureBytes)
                        )
                    ),
                    errors = mapOf("org.iso.18013.5.1" to 1)
                )
            ),
            status = 0u
        )

        val encoded = mapper.writeValueAsBytes(original)
        val decoded = mapper.readTree(encoded)

        assertEquals(1, decoded["documents"][0]["errors"]["org.iso.18013.5.1"].asInt())
    }

    @Test
    fun `documentErrors field serializes to CBOR correctly`() {
        val deviceResponse = DeviceResponseDto.DeviceResponseDTO(
            status = 0u,
            documentErrors = mapOf(docType to 10u)
        )

        val encoded = mapper.writeValueAsBytes(deviceResponse)
        val encodedString = encoded.joinToString("", transform = hexFormatter)

        assertContains(
            charSequence = encodedString,
            other = "6e646f63756d656e744572726f7273" // "documentErrors" UTF-8 in hex
        )

        val decoded = mapper.readTree(encoded)
        assertEquals(10, decoded["documentErrors"][docType].asInt())
    }

    @Test
    fun `IssuerSignedItemDeserializer deserializes elementValue`(
        @TestParameter elementType: ElementValueType
    ) {
        val encoded = mapper.writeValueAsBytes(issuerSignedItemMap(elementType.input))
        val result = mapper.readValue(
            encoded,
            DeviceResponseDto.IssuerSignedItemDTO::class.java
        )

        assertEquals(1L, result.digestId)
        assertEquals("family_name", result.elementIdentifier)
        assertEquals(elementType.expected, result.elementValue)
    }

    @Suppress("unused")
    enum class ElementValueType(val input: Any, val expected: Any) {
        TEXT("Smith", "Smith"),
        BOOLEAN(true, true),
        NUMBER(42, 42),
        COMPLEX(mapOf("nested" to "value"), "{\"nested\":\"value\"}")
    }

    private fun issuerSignedItemMap(elementValue: Any): Map<String, Any> = mapOf(
        "digestID" to 1L,
        "random" to byteArrayOf(0x01, 0x02, 0x03),
        "elementIdentifier" to "family_name",
        "elementValue" to elementValue
    )

    /**
     * ISO 18013-5 8.3: For any cryptographic
     * operation, an mdoc, mdoc reader or issuing authority infrastructure shall use these
     * bytestrings as they were sent or received, without attempting to re-create them from the
     * underlying maps.
     *
     * IssuerSignedItemBytes = #6.24(bstr .cbor IssuerSignedItem)
     *
     * After deserialization, the domain model's nameSpaces ByteArrays must include the
     * Tag 24 envelope so that digest verification can hash the complete IssuerSignedItemBytes.
     */
    @Test
    fun `deserialized IssuerSigned nameSpaces preserves Tag 24 encoded IssuerSignedItemBytes`() {
        val innerItemBytes = byteArrayOf(0xA4.toByte(), 0x01, 0x02, 0x03, 0x04)
        val issuerSignedItemBytes = wrapTag24(innerItemBytes)

        val original = DeviceResponseDtoStub.deviceResponseDto(
            nameSpaces = mapOf(
                "org.iso.18013.5.1" to listOf(EmbeddedCbor(innerItemBytes))
            )
        )

        val encoded = mapper.writeValueAsBytes(original)
        val decoded = mapper.readValue(encoded, DeviceResponseDto.DeviceResponseDTO::class.java)
        val itemBytes = decoded.toDomain(encoded).documents!!.first()
            .issuerSigned.nameSpaces!!["org.iso.18013.5.1"]!![0]

        assertThat(
            "Domain bytes must be full IssuerSignedItemBytes with Tag 24 prefix (0xD8 0x18)",
            itemBytes,
            equalTo(issuerSignedItemBytes)
        )
    }

    /**
     * ISO 18013-5 §9.1.3: The deviceSignature COSE_Sign1 protected header uses integer map keys
     * (e.g., key 1 = algorithm). Jackson re-encoding converts these to string keys, corrupting
     * the Sig_structure and causing signature verification to fail.
     *
     * This test verifies that toDomain preserves the original deviceSignature bytes using
     * offset-based extraction, not Jackson re-encoding.
     */
    @Test
    fun `toDomain preserves deviceSignature raw bytes with integer map keys`() {
        // Build a COSE_Sign1 array with integer key in protected header: {1: -7}
        val protectedHeader = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(1)
                gen.writeFieldId(1)
                gen.writeNumber(-7L)
                gen.writeEndObject()
            }
        }.toByteArray()

        val coseSign1 = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartArray(null, 4)
                gen.writeBinary(protectedHeader)
                gen.writeStartObject(0)
                gen.writeEndObject()
                gen.writeNull()
                gen.writeBinary(byteArrayOf(0x01, 0x02, 0x03))
                gen.writeEndArray()
            }
        }.toByteArray()

        val original = DeviceResponseDto.DeviceResponseDTO(
            version = "1.0",
            documents = listOf(
                DeviceResponseDto.DocumentDTO(
                    docType = docType,
                    issuerSigned = DeviceResponseDto.IssuerSignedDTO(
                        nameSpaces = null,
                        issuerAuth = RawCbor(mapper.writeValueAsBytes(listOf<Any>()))
                    ),
                    deviceSigned = DeviceResponseDto.DeviceSignedDTO(
                        nameSpaces = EmbeddedCbor(deviceNameSpacesData),
                        deviceAuth = DeviceResponseDto.DeviceAuthDTO(
                            deviceSignature = RawCbor(coseSign1)
                        )
                    )
                )
            ),
            status = 0u
        )

        val encoded = mapper.writeValueAsBytes(original)
        val decoded = mapper.readValue(encoded, DeviceResponseDto.DeviceResponseDTO::class.java)
        val domain = decoded.toDomain(encoded)

        assertThat(
            "deviceSignature must preserve original COSE_Sign1 bytes with integer map keys",
            domain.documents!!.first().deviceSigned.deviceSignature,
            equalTo(coseSign1)
        )
    }

    private fun wrapTag24(content: ByteArray): ByteArray = ByteArrayOutputStream().also { out ->
        CBORFactory().createGenerator(out).use { gen ->
            gen.writeTag(24)
            gen.writeBinary(content)
        }
    }.toByteArray()

    private fun generateBytesTag(elementSize: Int) = hexFormatter(PREFIX_TYPE_BYTES + elementSize)
}
