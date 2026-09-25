package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import kotlin.test.Test
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper

class DocRequestDtoTest {

    private val dto = DocRequestDto(
        itemsRequest = ItemsRequestDto(
            docType = "Unit test",
            nameSpaces = mapOf(
                "unit" to mapOf("test" to true)
            )
        )
    )

    private val differentAuth = dto.copy(
        readerAuth = byteArrayOf(1, 2)
    )

    private val differentItemsRequest = dto.copy(
        itemsRequest = dto.itemsRequest.copy(
            docType = "Different test"
        )
    )

    @Suppress("EqualsNullCall")
    @Test
    fun `Equality contract`() {
        assertEquals(dto, dto)
        assertEquals(dto, dto.copy())

        assertFalse(dto.equals(null))
        assertFalse(dto.equals("different type"))
        assertNotEquals(dto, differentItemsRequest)
        assertNotEquals(dto, differentAuth)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(dto.hashCode(), dto.hashCode())
        assertEquals(dto.hashCode(), dto.copy().hashCode())

        assertNotEquals(dto.hashCode(), differentAuth.hashCode())
        assertNotEquals(dto.hashCode(), differentItemsRequest.hashCode())
    }

    @Test
    fun `Serialization process`() {
        val serialized = CborMapper.default.writeValueAsBytes(dto)
        val result = CborMapper.default.readValue(serialized, DocRequestDto::class.java)

        assertThat(
            result,
            equalTo(dto)
        )
    }
}
