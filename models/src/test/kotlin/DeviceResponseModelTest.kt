import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Document
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSigned
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.IssuerSignedItem

class DeviceResponseModelTest {

    private val namespace = "org.iso.18013.5.1"
    private val docType = "org.iso.18013.5.1.mDL"

    private val familyNameRandom = "8798645B20EA200E19FFABAC92624BEE6AEC63ACEEDECFB1B80077D22BFC20E9"
        .chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private val portraitRandom = "D094DAD764A2EB9DEB5210E9D899643EFBD1D069CC311D3295516CA0B024412D"
        .chunked(2).map { it.toInt(16).toByte() }.toByteArray()

    private val portraitBytes = "FFD8FFE000104A464946000101010090009000".chunked(2)
        .map { it.toInt(16).toByte() }.toByteArray()

    private val emptyNameSpacesBytes = byteArrayOf(0xA0.toByte()) // CBOR {}

    private val model = DeviceResponse(
        version = "1.0",
        documents = listOf(
            Document(
                docType = docType,
                issuerSigned = IssuerSigned(
                    nameSpaces = mapOf(
                        namespace to listOf(
                            IssuerSignedItem(
                                digestId = 0,
                                random = familyNameRandom,
                                elementIdentifier = "family_name",
                                elementValue = "Smith"
                            ),
                            IssuerSignedItem(
                                digestId = 8,
                                random = portraitRandom,
                                elementIdentifier = "portrait",
                                elementValue = portraitBytes
                            )
                        )
                    ),
                    issuerAuth = byteArrayOf()
                ),
                deviceSigned = DeviceSigned(
                    nameSpaces = emptyNameSpacesBytes,
                    deviceSignature = byteArrayOf()
                )
            )
        ),
        documentErrors = null,
        status = 0
    )

    @Test
    fun `DeviceResponse has correct version and status`() {
        assertEquals("1.0", model.version)
        assertEquals(0L, model.status)
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
        assertEquals(2, issuerSigned.nameSpaces[namespace]!!.size)
    }

    @Test
    fun `IssuerSignedItem family_name has correct fields`() {
        val item = model.documents!!.first().issuerSigned.nameSpaces!![namespace]!![0]
        assertEquals(0L, item.digestId)
        assertEquals("family_name", item.elementIdentifier)
        assertEquals("Smith", item.elementValue)
        assertArrayEquals(familyNameRandom, item.random)
    }

    @Test
    fun `IssuerSignedItem portrait has correct fields`() {
        val item = model.documents!!.first().issuerSigned.nameSpaces!![namespace]!![1]
        assertEquals(8L, item.digestId)
        assertEquals("portrait", item.elementIdentifier)
        assertArrayEquals(portraitBytes, item.elementValue as ByteArray)
        assertArrayEquals(portraitRandom, item.random)
    }

    @Test
    fun `DeviceSigned nameSpaces is empty CBOR map`() {
        val deviceSigned = model.documents!!.first().deviceSigned
        assertArrayEquals(emptyNameSpacesBytes, deviceSigned.nameSpaces)
    }
}
