package uk.gov.onelogin.sharing.cryptoService.cbor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_DOC_TYPE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.MDL_NAMESPACE
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.over18Request
import uk.gov.onelogin.sharing.cryptoService.cbor.ItemsRequestEncoderStub.over21Request
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest.ItemsRequestDto

class ItemsRequestEncoderTest {

    private fun roundtrip(dto: ItemsRequestDto): ItemsRequestDto =
        CborMapper.default.readValue(dto.toCbor(), ItemsRequestDto::class.java)

    @Test
    fun `toCbor decodes back to original docType`() {
        assertEquals(MDL_DOC_TYPE, roundtrip(over21Request.toDto()).docType)
    }

    @Test
    fun `toCbor decodes back to original nameSpaces`() {
        assertEquals(
            over21Request.nameSpaces[MDL_NAMESPACE],
            roundtrip(over21Request.toDto()).nameSpaces[MDL_NAMESPACE]
        )
    }

    @Test
    fun `toCbor maps portrait and age_over_21 with intentToRetain false`() {
        val elements = roundtrip(over21Request.toDto()).nameSpaces[MDL_NAMESPACE]!!

        assertEquals(false, elements["portrait"])
        assertEquals(false, elements["age_over_21"])
        assertFalse(elements.containsKey("given_name"))
        assertFalse(elements.containsKey("family_name"))
        assertEquals(2, elements.size)
    }

    @Test
    fun `toCbor maps given_name and family_name retain true and age_over_18 false`() {
        val elements = roundtrip(over18Request.toDto()).nameSpaces[MDL_NAMESPACE]!!

        assertEquals(true, elements["given_name"])
        assertEquals(true, elements["family_name"])
        assertEquals(false, elements["age_over_18"])
        assertFalse(elements.containsKey("portrait"))
        assertEquals(3, elements.size)
    }

    @Test
    fun `toCbor preserves namespace key`() {
        assertTrue(roundtrip(over21Request.toDto()).nameSpaces.containsKey(MDL_NAMESPACE))
    }

    @Test
    fun `toDto rejects empty docType`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            over21Request.copy(docType = "").toDto()
        }
    }

    @Test
    fun `toDto rejects empty nameSpaces`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            over21Request.copy(nameSpaces = emptyMap()).toDto()
        }
    }
}
