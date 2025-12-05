package uk.gov.onelogin.sharing.security.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.security.Security
import uk.gov.onelogin.sharing.security.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.security.SecurityTestStub.SECURITY_EXPECTED_BASE64
import uk.gov.onelogin.sharing.security.SecurityTestStub.securityNodes
import uk.gov.onelogin.sharing.security.cbor.serializers.SecuritySerializer

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
}
