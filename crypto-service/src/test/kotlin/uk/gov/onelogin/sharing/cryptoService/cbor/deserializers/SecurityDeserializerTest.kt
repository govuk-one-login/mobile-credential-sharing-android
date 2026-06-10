package uk.gov.onelogin.sharing.cryptoService.cbor.deserializers

import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.SecurityDeserializerStub.expectedSecurityDto
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.security.SecurityDto

class SecurityDeserializerTest {

    @Test
    fun `maps SecurityDto via CborMapper`() {
        val bytes = CborMapper.default.writeValueAsBytes(expectedSecurityDto)
        val actual = CborMapper.default.readValue(bytes, SecurityDto::class.java)

        assertEquals(expectedSecurityDto, actual)
    }
}
