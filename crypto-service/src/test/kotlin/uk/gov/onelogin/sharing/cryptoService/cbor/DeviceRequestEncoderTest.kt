package uk.gov.onelogin.sharing.cryptoService.cbor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.CBOR_TAG_24_BYTE_0
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.CBOR_TAG_24_BYTE_1
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_NAMESPACE
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecoderImpl

class DeviceRequestEncoderTest {

    private val decoder = DeviceRequestDecoderImpl(SystemLogger())

    @Test
    fun `DeviceRequest encodeCbor sets version to 1_0`() {
        assertEquals(
            "1.0",
            decoder.deviceRequestDecoder(deviceRequestStub.encodeCbor()).version
        )
    }

    @Test
    fun `DeviceRequest encodeCbor produces exactly one docRequest`() {
        assertEquals(
            1,
            decoder.deviceRequestDecoder(deviceRequestStub.encodeCbor()).docRequests.size
        )
    }

    @Test
    fun `DeviceRequest encodeCbor itemsRequest is wrapped in Tag 24`() {
        val encoded = deviceRequestStub.encodeCbor()
        val tag24Sequence = byteArrayOf(CBOR_TAG_24_BYTE_0.toByte(), CBOR_TAG_24_BYTE_1.toByte())

        assertTrue(encoded.toList().windowed(2).any { it == tag24Sequence.toList() })
    }

    @Test
    fun `DeviceRequest encodeCbor itemsRequest decodes to correct docType`() {
        val decoded = decoder.deviceRequestDecoder(deviceRequestStub.encodeCbor())

        assertEquals(MDL_DOC_TYPE, decoded.docRequests.first().itemsRequest.docType)
    }

    @Test
    fun `DeviceRequest encodeCbor itemsRequest decodes to correct namespace elements`() {
        val decoded = decoder.deviceRequestDecoder(deviceRequestStub.encodeCbor())

        assertEquals(
            mapOf("age_over_18" to false),
            decoded.docRequests.first().itemsRequest.nameSpaces[MDL_NAMESPACE]
        )
    }
}
