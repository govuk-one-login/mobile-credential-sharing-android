package uk.gov.onelogin.sharing.cryptoService.cbor

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSignedItem
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status

class DeviceResponseMapperTest {

    private val docType = "org.iso.18013.5.1.mDL"
    private val namespace = "org.iso.18013.5.1"

    @Test
    fun `maps DeviceResponse domain model to DTO`() {
        val issuerAuth = byteArrayOf(0x01, 0x02)
        val nameSpacesBytes = byteArrayOf(0x03, 0x04)
        val randomBytes = byteArrayOf(0x05, 0x06)

        val domainModel = DeviceResponse(
            version = "1.0",
            documents = listOf(
                Document(
                    docType = docType,
                    issuerSigned = IssuerSigned(
                        nameSpaces = mapOf(
                            namespace to listOf(
                                IssuerSignedItem(
                                    digestId = 1,
                                    random = randomBytes,
                                    elementIdentifier = "family_name",
                                    elementValue = "Doe"
                                )
                            )
                        ),
                        issuerAuth = issuerAuth
                    ),
                    deviceSigned = DeviceSigned(
                        nameSpaces = nameSpacesBytes,
                        deviceSignature = byteArrayOf(0x07, 0x08)
                    )
                )
            ),
            documentErrors = mapOf("errorDoc" to Status.GENERAL_ERROR),
            status = Status.OK
        )

        val dto = domainModel.toDto()

        assertEquals("1.0", dto.version)
        assertEquals(Status.OK.code, dto.status)
        assertEquals(1, dto.documents?.size)
        assertEquals(1, dto.documentErrors?.size)
        assertEquals(Status.GENERAL_ERROR.code, dto.documentErrors?.get("errorDoc"))

        val documentDto = dto.documents!![0]
        assertEquals(docType, documentDto.docType)

        assertNotNull(documentDto.issuerSigned.nameSpaces)
        val issuerItems = documentDto.issuerSigned.nameSpaces!![namespace]
        assertEquals(1, issuerItems?.size)
        assertNotNull(issuerItems!![0].encoded)

        assertArrayEquals(nameSpacesBytes, documentDto.deviceSigned.nameSpaces.encoded)
        assertArrayEquals(
            byteArrayOf(0x07, 0x08),
            documentDto.deviceSigned.deviceAuth.deviceSignature
        )
    }

    @Test
    fun `maps null documents and documentErrors correctly`() {
        val domainModel = DeviceResponse(
            documents = null,
            documentErrors = null,
            status = Status.CBOR_DECODING_ERROR
        )

        val dto = domainModel.toDto()

        assertNull(dto.documents)
        assertNull(dto.documentErrors)
        assertEquals(Status.CBOR_DECODING_ERROR.code, dto.status)
    }

    @Test
    fun `toEmbeddedCbor for IssuerSignedItem encodes correctly`() {
        val randomBytes = byteArrayOf(0x01, 0x02)
        val item = IssuerSignedItem(
            digestId = 1,
            random = randomBytes,
            elementIdentifier = "given_name",
            elementValue = "Jane"
        )

        val embeddedCbor = item.toEmbeddedCbor()

        assertNotNull(embeddedCbor.encoded)
        assertTrue(embeddedCbor.encoded.isNotEmpty())
    }
}
