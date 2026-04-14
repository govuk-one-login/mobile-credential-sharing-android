package uk.gov.onelogin.sharing.cryptoService.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.SECURITY_EXPECTED_BASE64
import uk.gov.onelogin.sharing.cryptoService.SecurityTestStub.securityNodes
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.SecuritySerializer
import uk.gov.onelogin.sharing.models.mdoc.security.Security

class SecurityTest {
    private fun testMapper(): ObjectMapper = CBORMapper.builder(CBORFactory())
        .addModule(KotlinModule.Builder().build())
        .addModule(
            SimpleModule().apply {
                addSerializer(Security::class.java, SecuritySerializer())
            }
        )
        .build()

    @Test
    fun `encode Security to expected base64 string`() {
        val encoded = testMapper().writeValueAsBytes(SECURITY)
        val base64 = Base64.getEncoder().encodeToString(encoded)

        assertEquals(SECURITY_EXPECTED_BASE64, base64)
    }

    @Test
    fun `encode Security to expected json structure`() {
        val mapper = testMapper()
        val cborBytes = mapper.writeValueAsBytes(SECURITY)
        val actualNode = mapper.readTree(cborBytes)

        val expectedSecurity = securityNodes()

        assertEquals(
            "CBOR structure should match expected JSON",
            expectedSecurity,
            actualNode
        )
    }

    // ISO 18013-5: Security array must use definite-length encoding
    @Test
    fun `Security encodes to definite-length array`() {
        val encoded = testMapper().writeValueAsBytes(SECURITY)

        // 82 = definite-length array with 2 elements
        assertEquals(0x82.toByte(), encoded[0])
    }
}
