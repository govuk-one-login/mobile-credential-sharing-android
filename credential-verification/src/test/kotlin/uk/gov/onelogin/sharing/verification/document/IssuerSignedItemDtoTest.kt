package uk.gov.onelogin.sharing.verification.document

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class IssuerSignedItemDtoTest {

    private val cborMapper = ObjectMapper(CBORFactory())

    @Test
    fun `deserializes valid IssuerSignedItem bytes`() {
        val bytes = buildItemBytes("family_name", "Smith")
        val dto = cborMapper.readValue(bytes, IssuerSignedItemDto::class.java)

        assertThat(dto.digestId, equalTo(1L))
        assertNotNull(dto.random)
        assertThat(dto.elementIdentifier, equalTo("family_name"))
        assertThat(dto.elementValue?.toString(), equalTo("Smith"))
    }

    @Test
    fun `deserializes item with integer elementValue`() {
        val bytes = buildItemBytesWithIntValue("age", 30)
        val dto = cborMapper.readValue(bytes, IssuerSignedItemDto::class.java)

        assertThat(dto.elementIdentifier, equalTo("age"))
        assertThat(dto.elementValue?.toString(), equalTo("30"))
    }

    @Test
    fun `deserializes item with boolean elementValue`() {
        val bytes = buildItemBytesWithBoolValue("age_over_18", true)
        val dto = cborMapper.readValue(bytes, IssuerSignedItemDto::class.java)

        assertThat(dto.elementIdentifier, equalTo("age_over_18"))
        assertThat(dto.elementValue?.toString(), equalTo("true"))
    }

    @Test
    fun `throws for missing required fields`() {
        val node = cborMapper.createObjectNode()
        node.put("digestID", 0)
        val bytes = cborMapper.writeValueAsBytes(node)

        assertThrows(Exception::class.java) {
            cborMapper.readValue(bytes, IssuerSignedItemDto::class.java)
        }
    }

    private fun buildItemBytes(identifier: String, value: String): ByteArray {
        val node = cborMapper.createObjectNode()
        node.put(IssuerSignedItemDto.KEY_DIGEST_ID, 1)
        node.put(IssuerSignedItemDto.KEY_RANDOM, byteArrayOf(0x01, 0x02))
        node.put(IssuerSignedItemDto.KEY_ELEMENT_IDENTIFIER, identifier)
        node.put(IssuerSignedItemDto.KEY_ELEMENT_VALUE, value)
        return cborMapper.writeValueAsBytes(node)
    }

    private fun buildItemBytesWithIntValue(identifier: String, value: Int): ByteArray {
        val node = cborMapper.createObjectNode()
        node.put(IssuerSignedItemDto.KEY_DIGEST_ID, 1)
        node.put(IssuerSignedItemDto.KEY_RANDOM, byteArrayOf(0x01))
        node.put(IssuerSignedItemDto.KEY_ELEMENT_IDENTIFIER, identifier)
        node.put(IssuerSignedItemDto.KEY_ELEMENT_VALUE, value)
        return cborMapper.writeValueAsBytes(node)
    }

    private fun buildItemBytesWithBoolValue(identifier: String, value: Boolean): ByteArray {
        val node = cborMapper.createObjectNode()
        node.put(IssuerSignedItemDto.KEY_DIGEST_ID, 1)
        node.put(IssuerSignedItemDto.KEY_RANDOM, byteArrayOf(0x01))
        node.put(IssuerSignedItemDto.KEY_ELEMENT_IDENTIFIER, identifier)
        node.put(IssuerSignedItemDto.KEY_ELEMENT_VALUE, value)
        return cborMapper.writeValueAsBytes(node)
    }
}
