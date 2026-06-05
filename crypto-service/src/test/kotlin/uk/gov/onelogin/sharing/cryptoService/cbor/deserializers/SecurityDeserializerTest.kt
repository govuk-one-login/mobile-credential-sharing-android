package uk.gov.onelogin.sharing.cryptoService.cbor.deserializers

import kotlin.test.assertNotNull
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.SecurityDeserializerStub.expectedCoseKey
import uk.gov.onelogin.sharing.cryptoService.SecurityDeserializerStub.expectedSecurityDto
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SecurityDto

class SecurityDeserializerTest {

    @Test
    fun `maps SecurityDto via CborMapper`() {
        val bytes = CborMapper.default.writeValueAsBytes(expectedSecurityDto)
        val actual = CborMapper.default.readValue(bytes, SecurityDto::class.java)
        val actualKey = assertNotNull(actual.ephemeralPublicKey)

        assertEquals(expectedSecurityDto.cipherSuiteIdentifier, actual.cipherSuiteIdentifier)
        assertEquals(expectedCoseKey.keyType, actualKey.keyType)
        assertEquals(expectedCoseKey.curve, actualKey.curve)
        assertArrayEquals(expectedCoseKey.x, actualKey.x)
        assertArrayEquals(expectedCoseKey.y, actualKey.y)
    }
}
