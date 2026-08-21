package uk.gov.onelogin.sharing.cryptoService.cbor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.CBOR_TAG_24_BYTE_0
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.CBOR_TAG_24_BYTE_1
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_NAMESPACE
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecoderImpl
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DocRequest
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequest

class DeviceRequestEncoderTest {

    private val decoder = DeviceRequestDecoderImpl(SystemLogger())

    @Test
    fun `DeviceRequest encodeCbor sets version to 1_0`() {
        assertEquals(
            "1.0",
            decoder.deviceRequestDecoder(deviceRequestStub.toDto().toCbor()).version
        )
    }

    @Test
    fun `DeviceRequest encodeCbor produces exactly one docRequest`() {
        assertEquals(
            1,
            decoder.deviceRequestDecoder(deviceRequestStub.toDto().toCbor()).docRequests.size
        )
    }

    @Test
    fun `DeviceRequest encodeCbor itemsRequest is wrapped in Tag 24`() {
        val encoded = deviceRequestStub.toDto().toCbor()
        val tag24Sequence = byteArrayOf(CBOR_TAG_24_BYTE_0.toByte(), CBOR_TAG_24_BYTE_1.toByte())

        assertTrue(encoded.toList().windowed(2).any { it == tag24Sequence.toList() })
    }

    @Test
    fun `DeviceRequest encodeCbor itemsRequest decodes to correct docType`() {
        val decoded = decoder.deviceRequestDecoder(deviceRequestStub.toDto().toCbor())

        assertEquals(MDL_DOC_TYPE, decoded.docRequests.first().itemsRequest.docType)
    }

    @Test
    fun `DeviceRequest encodeCbor itemsRequest decodes to correct namespace elements`() {
        val decoded = decoder.deviceRequestDecoder(deviceRequestStub.toDto().toCbor())

        assertEquals(
            mapOf("portrait" to false, "age_over_18" to false),
            decoded.docRequests.first().itemsRequest.nameSpaces[MDL_NAMESPACE]
        )
    }

    @Test
    fun `DeviceRequest encodeCbor readerAuth is encoded as raw CBOR array`() {
        val itemsRequest =
            ItemsRequest(MDL_DOC_TYPE, mapOf(MDL_NAMESPACE to mapOf("portrait" to true)))
        val coseSign1Array = byteArrayOf(
            0x84.toByte(),
            0x43,
            0xA1.toByte(),
            0x01,
            0x26,
            0xA0.toByte(),
            0xF6.toByte(),
            0x44,
            0xDE.toByte(),
            0xAD.toByte(),
            0xBE.toByte(),
            0xEF.toByte()
        )

        val docRequest = DocRequest(itemsRequest, readerAuth = coseSign1Array)
        val deviceRequest =
            uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.DeviceRequest(
                "1.0",
                listOf(docRequest)
            )

        val encoded = deviceRequest.toDto().toCbor()

        assertTrue(encoded.toHexString().contains(coseSign1Array.toHexString()))
        assertFalse(encoded.toHexString().contains("d818" + coseSign1Array.toHexString()))
    }
}
