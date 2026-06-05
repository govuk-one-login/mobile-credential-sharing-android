package uk.gov.onelogin.sharing.cryptoService.security

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.SECURITY_EXPECTED_BASE64
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.securityNodes
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.toDto

class SecurityTest {

    @Test
    fun `encode Security to expected base64 string`() {
        val encoded = CborMapper.default.writeValueAsBytes(SECURITY.toDto())
        val base64 = Base64.getEncoder().encodeToString(encoded)
        assertEquals(SECURITY_EXPECTED_BASE64, base64)
    }

    @Test
    fun `encode Security to expected json structure`() {
        val cborBytes = CborMapper.default.writeValueAsBytes(SECURITY.toDto())
        val actualNode = CborMapper.default.readTree(cborBytes)
        assertEquals("CBOR structure should match expected JSON", securityNodes(), actualNode)
    }

    // ISO 18013-5: Security array must use definite-length encoding
    @Test
    fun `Security encodes to definite-length array`() {
        val encoded = CborMapper.default.writeValueAsBytes(SECURITY.toDto())
        assertEquals(0x82.toByte(), encoded[0])
    }
}
