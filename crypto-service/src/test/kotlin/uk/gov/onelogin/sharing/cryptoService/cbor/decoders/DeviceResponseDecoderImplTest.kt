package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.toDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

@RunWith(TestParameterInjector::class)
class DeviceResponseDecoderImplTest {

    private val logger = SystemLogger()
    private val decoder = DeviceResponseDecoderImpl(logger)

    private val namespace = "org.iso.18013.5.1"
    private val docType = "org.iso.18013.5.1.mDL"

    private val emptyNameSpacesBytes = ByteArrayOutputStream().also { out ->
        CBORFactory().createGenerator(out).use { gen ->
            gen.writeStartObject(0)
            gen.writeEndObject()
        }
    }.toByteArray()

    private val issuerAuthBytes = byteArrayOf(0x84.toByte(), 0x01, 0x02, 0x03, 0x04)
    private val deviceAuthBytes = byteArrayOf(0x84.toByte(), 0x05, 0x06, 0x07, 0x08)
    private val itemBytes1 = byteArrayOf(0xA4.toByte(), 0x01, 0x02, 0x03)
    private val itemBytes2 = byteArrayOf(0xA4.toByte(), 0x04, 0x05, 0x06)

    private fun createDocument(
        docType: String = this.docType,
        items: List<ByteArray> = listOf(itemBytes1)
    ) = Document(
        docType = docType,
        issuerSigned = IssuerSigned(
            nameSpaces = mapOf(namespace to items),
            issuerAuth = issuerAuthBytes
        ),
        deviceSigned = DeviceSigned(
            nameSpaces = emptyNameSpacesBytes,
            deviceAuth = deviceAuthBytes
        )
    )

    private fun createDeviceResponse(
        documents: List<Document>? = listOf(createDocument()),
        status: Status = Status.OK,
        documentErrors: Map<String, Status>? = null
    ) = DeviceResponse(
        version = "1.0",
        documents = documents,
        status = status,
        documentErrors = documentErrors
    )

    @Test
    fun `decodes successful DeviceResponse with documents`() {
        val original = createDeviceResponse(
            documents = listOf(createDocument(items = listOf(itemBytes1, itemBytes2)))
        )

        val encoded = original.toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        assertEquals("1.0", decoded.version)
        assertEquals(Status.OK, decoded.status)
        val documents = decoded.documents
        assertNotNull(documents)
        assertEquals(1, documents.size)
        assertEquals(docType, documents.first().docType)
    }

    @Test
    fun `decodes nameSpaces items as raw byte arrays`() {
        val encoded = createDeviceResponse().toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        val items = decoded.documents!!.first().issuerSigned.nameSpaces!![namespace]!!
        assertEquals(1, items.size)
        assertArrayEquals(itemBytes1, items[0])
    }

    @Test
    fun `decodes multiple documents`() {
        val original = createDeviceResponse(
            documents = listOf(
                createDocument(),
                createDocument(docType = "org.iso.18013.5.1.mID", items = listOf(itemBytes2))
            )
        )

        val encoded = original.toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        val documents = decoded.documents!!
        assertEquals(2, documents.size)
        assertEquals(docType, documents[0].docType)
        assertEquals("org.iso.18013.5.1.mID", documents[1].docType)
    }

    @Test
    fun `decodes DeviceResponse with null documents`() {
        val encoded = createDeviceResponse(documents = null).toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        assertEquals(Status.OK, decoded.status)
        assertNull(decoded.documents)
    }

    @Test
    fun `decodes error status codes`() {
        val encoded = createDeviceResponse(
            documents = null,
            status = Status.GENERAL_ERROR
        ).toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        assertEquals(Status.GENERAL_ERROR, decoded.status)
    }

    @Test
    @TestParameters(valuesProvider = MalformedPayloadsProvider::class)
    fun `throws DeviceResponseDecodingException for malformed payloads`(payload: ByteArray) {
        assertFailsWith<DeviceResponseDecodingException> {
            decoder.decode(payload)
        }
    }

    @Test
    fun `throws DeviceResponseDecodingException for malformed CBOR logs error`() {
        val malformedBytes = byteArrayOf(0xFF.toByte())

        assertFailsWith<DeviceResponseDecodingException> {
            decoder.decode(malformedBytes)
        }

        assert(logger.contains(DeviceResponseDecoderImpl.LOG_CBOR_DECODING_ERROR))
    }

    @Test
    fun `logs success message on successful decode`() {
        val encoded = createDeviceResponse(documents = null).toDto().encodeCbor()
        decoder.decode(encoded)

        assert(logger.contains("DeviceResponse decoded successfully"))
    }

    @Test
    fun `decodes successfully when payload contains unsupported optional fields`() {
        val encoded = createDeviceResponse(
            documentErrors = mapOf(docType to Status.GENERAL_ERROR)
        ).toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        assertEquals(Status.OK, decoded.status)
        assertEquals("1.0", decoded.version)
        assertNotNull(decoded.documents)
        assertEquals(1, decoded.documents!!.size)
        assertEquals(docType, decoded.documents!!.first().docType)
    }

    @Test
    fun `throws DeviceResponseDecodingException when docType is missing`() {
        val missingDocType = ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen ->
                gen.writeStartObject(3)
                gen.writeStringField("version", "1.0")
                gen.writeNumberField("status", 0)
                gen.writeFieldName("documents")
                gen.writeStartArray()
                gen.writeStartObject(0)
                gen.writeEndObject()
                gen.writeEndArray()
                gen.writeEndObject()
            }
        }.toByteArray()

        assertFailsWith<DeviceResponseDecodingException> {
            decoder.decode(missingDocType)
        }
    }
}

private class MalformedPayloadsProvider : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues> =
        listOf(
            "malformed CBOR" to byteArrayOf(0xFF.toByte()),
            "invalid status code" to buildCbor {
                writeStartObject(2)
                writeStringField("version", "1.0")
                writeNumberField("status", 13)
                writeEndObject()
            },
            "missing version" to buildCbor {
                writeStartObject(1)
                writeNumberField("status", 0)
                writeEndObject()
            },
            "missing status" to buildCbor {
                writeStartObject(1)
                writeStringField("version", "1.0")
                writeEndObject()
            }
        ).map { (name, bytes) ->
            TestParameters.TestParametersValues.builder()
                .name(name)
                .addParameter("payload", bytes)
                .build()
        }

    private fun buildCbor(block: CBORGenerator.() -> Unit): ByteArray =
        ByteArrayOutputStream().also { out ->
            CBORFactory().createGenerator(out).use { gen -> gen.block() }
        }.toByteArray()
}
