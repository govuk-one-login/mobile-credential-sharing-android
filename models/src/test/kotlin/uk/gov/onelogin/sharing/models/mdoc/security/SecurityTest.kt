package uk.gov.onelogin.sharing.models.mdoc.security

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.dataformat.cbor.CBORFactory
import tools.jackson.dataformat.cbor.CBORMapper
import tools.jackson.module.kotlin.KotlinModule
import uk.gov.onelogin.sharing.models.mdoc.EmbeddedCbor

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
        val fakeKeyBytes = "FAKE_EDEVICE_KEY".toByteArray()
        val security = Security(
            1,
            EmbeddedCbor(fakeKeyBytes)
        )

        val encoded = testMapper().writeValueAsBytes(security)
        val base64 = Base64.getEncoder().encodeToString(encoded)

        val expectedBase64 =
            "nwFQRkFLRV9FREVWSUNFX0tFWf8="
        assertEquals(expectedBase64, base64)
    }

    @Test
    fun `encode Security to expected json structure`() {
        val fakeKeyBytes = "FAKE_EDEVICE_KEY".toByteArray()
        val fakeCipherId = 1

        val security = Security(
            fakeCipherId,
            EmbeddedCbor(fakeKeyBytes)
        )

        val mapper = testMapper()
        val cborBytes = mapper.writeValueAsBytes(security)
        val actualNode = mapper.readTree(cborBytes)

        val jsonNodeFactory = JsonNodeFactory.instance
        val expectedSecurity = jsonNodeFactory.arrayNode()
            .add(fakeCipherId)
            .add(fakeKeyBytes)

        kotlin.test.assertEquals(
            expectedSecurity,
            actualNode,
            "CBOR structure should match expected JSON"
        )

        val json = JsonMapper.builder().build()
        val pretty = json.writerWithDefaultPrettyPrinter().writeValueAsString(actualNode)
        println(pretty)
    }
}
