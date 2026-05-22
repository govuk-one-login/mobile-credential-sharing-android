package uk.gov.onelogin.sharing.cryptoService.cbor.decoders

import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.cbor.encodeCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.toDto
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

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

    @Test
    fun `decodes successful DeviceResponse with documents`() {
        val original = DeviceResponse(
            version = "1.0",
            documents = listOf(
                Document(
                    docType = docType,
                    issuerSigned = IssuerSigned(
                        nameSpaces = mapOf(namespace to listOf(itemBytes1, itemBytes2)),
                        issuerAuth = issuerAuthBytes
                    ),
                    deviceSigned = DeviceSigned(
                        nameSpaces = emptyNameSpacesBytes,
                        deviceAuth = deviceAuthBytes
                    )
                )
            ),
            status = Status.OK
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
        val original = DeviceResponse(
            version = "1.0",
            documents = listOf(
                Document(
                    docType = docType,
                    issuerSigned = IssuerSigned(
                        nameSpaces = mapOf(namespace to listOf(itemBytes1)),
                        issuerAuth = issuerAuthBytes
                    ),
                    deviceSigned = DeviceSigned(
                        nameSpaces = emptyNameSpacesBytes,
                        deviceAuth = deviceAuthBytes
                    )
                )
            ),
            status = Status.OK
        )

        val encoded = original.toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        val items = decoded.documents!!.first().issuerSigned.nameSpaces!![namespace]!!
        assertEquals(1, items.size)
        assertArrayEquals(itemBytes1, items[0])
    }

    @Test
    fun `decodes multiple documents`() {
        val original = DeviceResponse(
            version = "1.0",
            documents = listOf(
                Document(
                    docType = docType,
                    issuerSigned = IssuerSigned(
                        nameSpaces = mapOf(namespace to listOf(itemBytes1)),
                        issuerAuth = issuerAuthBytes
                    ),
                    deviceSigned = DeviceSigned(
                        nameSpaces = emptyNameSpacesBytes,
                        deviceAuth = deviceAuthBytes
                    )
                ),
                Document(
                    docType = "org.iso.18013.5.1.mID",
                    issuerSigned = IssuerSigned(
                        nameSpaces = mapOf(namespace to listOf(itemBytes2)),
                        issuerAuth = issuerAuthBytes
                    ),
                    deviceSigned = DeviceSigned(
                        nameSpaces = emptyNameSpacesBytes,
                        deviceAuth = deviceAuthBytes
                    )
                )
            ),
            status = Status.OK
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
        val original = DeviceResponse(
            version = "1.0",
            documents = null,
            status = Status.OK
        )

        val encoded = original.toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        assertEquals(Status.OK, decoded.status)
        assertNull(decoded.documents)
    }

    @Test
    fun `decodes error status codes`() {
        val original = DeviceResponse(
            version = "1.0",
            documents = null,
            status = Status.GENERAL_ERROR
        )

        val encoded = original.toDto().encodeCbor()
        val decoded = decoder.decode(encoded)

        assertEquals(Status.GENERAL_ERROR, decoded.status)
    }

    @Test
    fun `throws DeviceResponseDecodingException for malformed CBOR`() {
        val malformedBytes = byteArrayOf(0xFF.toByte())

        assertFailsWith<DeviceResponseDecodingException> {
            decoder.decode(malformedBytes)
        }

        assert(logger.contains(DeviceResponseDecoderImpl.LOG_CBOR_DECODING_ERROR))
    }

    @Test
    fun `throws DeviceResponseDecodingException for invalid status code`() {
        val invalidStatus = byteArrayOf(
            0xA2.toByte(),       // map(2)
            0x67,                // text(7)
            0x76, 0x65, 0x72, 0x73, 0x69, 0x6F, 0x6E, // "version"
            0x63,                // text(3)
            0x31, 0x2E, 0x30,   // "1.0"
            0x66,                // text(6)
            0x73, 0x74, 0x61, 0x74, 0x75, 0x73, // "status"
            0x0D                 // uint(13) - invalid
        )

        assertFailsWith<DeviceResponseDecodingException> {
            decoder.decode(invalidStatus)
        }
    }

    @Test
    fun `logs success message on successful decode`() {
        val original = DeviceResponse(
            version = "1.0",
            documents = null,
            status = Status.OK
        )

        val encoded = original.toDto().encodeCbor()
        decoder.decode(encoded)

        assert(logger.contains("DeviceResponse decoded successfully"))
    }
}
