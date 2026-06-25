package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper

class ItemsRequestDtoTest {

    private val dto = ItemsRequestDto(
        docType = "Unit test",
        nameSpaces = mapOf(
            "unit" to mapOf("test" to true)
        )
    )

    private val differentDocType = dto.copy(
        docType = "Another test"
    )

    private val differentNamespaces = dto.copy(
        nameSpaces = mapOf(
            "another" to mapOf("test" to true)
        )
    )

    private val differentRequestInfo = dto.copy(
        requestInfo = byteArrayOf(1, 2)
    )

    @Suppress("EqualsNullCall")
    @Test
    fun `Equality contract`() {
        assertEquals(dto, dto)
        assertEquals(dto, dto.copy())

        assertFalse(dto.equals(null))
        assertFalse(dto.equals("different type"))
        assertNotEquals(dto, differentDocType)
        assertNotEquals(dto, differentNamespaces)
        assertNotEquals(dto, differentRequestInfo)
    }

    @Test
    fun `Hashcode contract`() {
        assertEquals(dto.hashCode(), dto.hashCode())
        assertEquals(dto.hashCode(), dto.copy().hashCode())

        assertNotEquals(dto.hashCode(), differentDocType.hashCode())
        assertNotEquals(dto.hashCode(), differentNamespaces.hashCode())
        assertNotEquals(dto.hashCode(), differentRequestInfo.hashCode())
    }

    @Test
    fun `Serialization process`() {
        val serialized = CborMapper.default.writeValueAsBytes(dto)
        val result = CborMapper.default.readValue(serialized, ItemsRequestDto::class.java)

        MatcherAssert.assertThat(
            result,
            equalTo(dto)
        )
    }
}
