package uk.gov.onelogin.sharing.cryptoService.verifier

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

class DocRequestBuilderImplTest {

    private lateinit var builder: DocRequestBuilderImpl
    private val sampleReaderAuth = byteArrayOf(0x01, 0x02)
    private val sampleItemsRequestBytes = byteArrayOf(0x03, 0x04)
    private val mockBuildItemsBytes: (ItemsRequest) -> ByteArray = { byteArrayOf(0x05, 0x06) }

    @Before
    fun setUp() {
        builder = DocRequestBuilderImpl()
    }

    @Test
    fun `single candidate request builds 1 docRequest`() {
        val itemsRequest = ItemsRequest(
            docType = "org.iso.18013.5.1.mDL",
            nameSpaces = mapOf("org.iso.18013.5.1" to mapOf("portrait" to false))
        )

        val result = builder.buildDocRequests(
            itemsRequest = itemsRequest,
            itemsRequestBytes = sampleItemsRequestBytes,
            readerAuth = sampleReaderAuth,
            buildItemsRequestBytes = mockBuildItemsBytes
        )

        assertEquals(1, result.size)
        assertEquals("org.iso.18013.5.1.mDL", result[0].itemsRequest.docType)
        assertEquals(sampleReaderAuth, result[0].readerAuth)
        assertEquals(sampleItemsRequestBytes, result[0].itemsRequestBytes)
    }

    @Test
    fun `multi unsupported and valid request builds 2 candidates`() {
        val itemsRequest = ItemsRequest(
            docType = DocRequestBuilderImpl.TYPE_MULTI_UNSUPPORTED_AND_VALID,
            nameSpaces = mapOf("org.iso.18013.5.1" to mapOf("portrait" to false))
        )

        val result = builder.buildDocRequests(
            itemsRequest = itemsRequest,
            itemsRequestBytes = sampleItemsRequestBytes,
            readerAuth = sampleReaderAuth,
            buildItemsRequestBytes = mockBuildItemsBytes
        )

        assertEquals(2, result.size)
        assertEquals("org.iso.18013.5.1.eVRC", result[0].itemsRequest.docType)
        assertEquals("org.iso.18013.5.1.mDL", result[1].itemsRequest.docType)
    }

    @Test
    fun `multi unsupported and valid normalizes custom namespace to MDL namespace`() {
        val itemsRequest = ItemsRequest(
            docType = DocRequestBuilderImpl.TYPE_MULTI_UNSUPPORTED_AND_VALID,
            nameSpaces = mapOf("MULTI_UNSUPPORTED_AND_VALID" to mapOf("portrait" to false))
        )

        val result = builder.buildDocRequests(
            itemsRequest = itemsRequest,
            itemsRequestBytes = sampleItemsRequestBytes,
            readerAuth = sampleReaderAuth,
            buildItemsRequestBytes = mockBuildItemsBytes
        )

        assertEquals(2, result.size)
        assertEquals("org.iso.18013.5.1.mDL", result[1].itemsRequest.docType)
        org.junit.Assert.assertTrue(result[1].itemsRequest.nameSpaces.containsKey("org.iso.18013.5.1"))
    }
}
