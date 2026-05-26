package uk.gov.onelogin.sharing.models.deviceResponse

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingDeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.SharingIssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

class DeviceResponseModelTest {

    private val namespace = "org.iso.18013.5.1"
    private val docType = "org.iso.18013.5.1.mDL"

    private val familyNameItemBytes = byteArrayOf(0x01, 0x02, 0x03)
    private val portraitItemBytes = byteArrayOf(0x04, 0x05, 0x06)
    private val emptyNameSpacesBytes = byteArrayOf(0xA0.toByte())

    private val model = DeviceResponse(
        version = "1.0",
        documents = listOf(
            Document(
                docType = docType,
                issuerSigned = SharingIssuerSigned(
                    nameSpaces = mapOf(
                        namespace to listOf(familyNameItemBytes, portraitItemBytes)
                    ),
                    issuerAuth = byteArrayOf()
                ),
                deviceSigned = SharingDeviceSigned(
                    deviceNameSpacesBytes = emptyNameSpacesBytes,
                    deviceSignature = byteArrayOf()
                )
            )
        ),
        documentErrors = null,
        status = Status.OK
    )

    @Test
    fun `DeviceResponse has correct version and status`() {
        assertEquals("1.0", model.version)
        assertEquals(Status.OK, model.status)
    }

    @Test
    fun `DeviceResponse version defaults to 1 0`() {
        val response = DeviceResponse(documents = null, documentErrors = null)
        assertEquals("1.0", response.version)
    }

    @Test
    fun `DeviceResponse status defaults to OK`() {
        val response = DeviceResponse()
        assertEquals(Status.OK, response.status)
        assertEquals(0u, response.status.code)
    }

    @Test
    fun `DeviceResponse status supports GENERAL_ERROR`() {
        val response =
            DeviceResponse(status = Status.GENERAL_ERROR)
        assertEquals(Status.GENERAL_ERROR, response.status)
        assertEquals(10u, response.status.code)
    }

    @Test
    fun `DeviceResponse status supports CBOR_DECODING_ERROR`() {
        val response = DeviceResponse(
            documents = null,
            documentErrors = null,
            status = Status.CBOR_DECODING_ERROR
        )
        assertEquals(Status.CBOR_DECODING_ERROR, response.status)
        assertEquals(11u, response.status.code)
    }

    @Test
    fun `DeviceResponse status supports CBOR_VALIDATION_ERROR`() {
        val response = DeviceResponse(
            documents = null,
            documentErrors = null,
            status = Status.CBOR_VALIDATION_ERROR
        )
        assertEquals(Status.CBOR_VALIDATION_ERROR, response.status)
        assertEquals(12u, response.status.code)
    }

    @Test
    fun `DeviceResponse has one document with correct docType`() {
        assertEquals(1, model.documents!!.size)
        assertEquals(docType, model.documents.first().docType)
    }

    @Test
    fun `DeviceResponse has no document errors`() {
        assertNull(model.documentErrors)
    }

    @Test
    fun `IssuerSigned nameSpaces contains expected namespace`() {
        val issuerSigned = model.documents!!.first().issuerSigned
        assertTrue(issuerSigned.nameSpaces!!.containsKey(namespace))
        assertEquals(2, issuerSigned.nameSpaces!![namespace]!!.size)
    }

    @Test
    fun `IssuerSigned nameSpaces items are raw bytes`() {
        val items = model.documents!!.first().issuerSigned.nameSpaces!![namespace]!!
        assertArrayEquals(familyNameItemBytes, items[0])
        assertArrayEquals(portraitItemBytes, items[1])
    }

    @Test
    fun `DeviceSigned nameSpaces is empty CBOR map`() {
        val deviceSigned = model.documents!!.first().deviceSigned
        assertArrayEquals(emptyNameSpacesBytes, deviceSigned.deviceNameSpacesBytes)
    }
}
