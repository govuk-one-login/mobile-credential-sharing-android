package uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceRequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertThrows
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

    @Test
    fun `toDomain maps to ItemsRequest`() {
        val domain = dto.toDomain()

        assertEquals("Unit test", domain.docType)
        assertEquals(mapOf("unit" to mapOf("test" to true)), domain.nameSpaces)
    }

    @Test
    fun `toDto maps ItemsRequest back to DTO`() {
        val domain = ItemsRequest(
            docType = "org.iso.18013.5.1.mDL",
            nameSpaces = mapOf("org.iso.18013.5.1" to mapOf("age_over_18" to false))
        )

        val result = domain.toDto()

        assertEquals(domain.docType, result.docType)
        assertEquals(domain.nameSpaces, result.nameSpaces)
    }

    @Test
    fun `empty docType throws IllegalArgumentException`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ItemsRequestDto(
                docType = "",
                nameSpaces = mapOf("ns" to mapOf("el" to true))
            )
        }

        assertEquals("ItemsRequest: docType must not be empty", exception.message)
    }

    @Test
    fun `empty nameSpaces throws IllegalArgumentException`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            ItemsRequestDto(
                docType = "org.iso.18013.5.1.mDL",
                nameSpaces = emptyMap()
            )
        }

        assertEquals("ItemsRequest: nameSpaces must not be empty", exception.message)
    }

    @Test
    fun `round-trip with multiple nameSpaces preserves all entries`() {
        val multiNamespaces = ItemsRequestDto(
            docType = "org.iso.18013.5.1.mDL",
            nameSpaces = mapOf(
                "org.iso.18013.5.1" to mapOf("family_name" to true, "age_over_18" to false),
                "org.iso.18013.5.1.aamva" to mapOf("DHS_compliance" to true)
            )
        )

        val serialized = CborMapper.default.writeValueAsBytes(multiNamespaces)
        val result = CborMapper.default.readValue(serialized, ItemsRequestDto::class.java)

        assertEquals(multiNamespaces, result)
    }
}
