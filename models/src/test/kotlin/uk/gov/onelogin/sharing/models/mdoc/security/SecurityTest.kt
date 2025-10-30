package uk.gov.onelogin.sharing.models.mdoc.security

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.dataformat.cbor.CBORFactory
import tools.jackson.dataformat.cbor.CBORMapper
import tools.jackson.module.kotlin.KotlinModule
import uk.gov.onelogin.sharing.models.SecurityTestStub.SECURITY
import uk.gov.onelogin.sharing.models.SecurityTestStub.SECURITY_EXPECTED_BASE64
import uk.gov.onelogin.sharing.models.SecurityTestStub.securityNodes

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

        val json = JsonMapper.builder().build()
        val pretty = json.writerWithDefaultPrettyPrinter().writeValueAsString(actualNode)
        println(pretty)
    }
}
