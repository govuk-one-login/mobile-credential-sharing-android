package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

import java.util.Base64
import junit.framework.TestCase.assertEquals
import kotlin.test.assertEquals
import org.junit.Test
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.databind.node.JsonNodeFactory
import tools.jackson.dataformat.cbor.CBORFactory
import tools.jackson.dataformat.cbor.CBORMapper
import tools.jackson.module.kotlin.KotlinModule
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCborSerializer

class BleOptionsTest {
    private val uuidBytes = "11111111-2222-3333-4444-555555555555".toByteArray()
    private val bleOptions = BleOptions(
        serverMode = true,
        clientMode = false,
        peripheralServerModeUuid = EmbeddedCbor(uuidBytes)
    )

    private fun testMapper(): ObjectMapper = CBORMapper.builder(CBORFactory())
        .addModule(KotlinModule.Builder().build())
        .addModule(
            SimpleModule().apply {
                addSerializer(EmbeddedCbor::class.java, EmbeddedCborSerializer())
                addSerializer(BleOptions::class.java, BleOptionsSerializer())
            }
        )
        .build()

    @Test
    fun `encode BleOptions to expected base64 string`() {
        val encoded = testMapper().writeValueAsBytes(bleOptions)
        val base64 = Base64.getEncoder().encodeToString(encoded)

        val expectedBase64 =
            "v2Ew9WEx9GIxMNgYWCQxMTExMTExMS0yMjIyLTMzMzMtNDQ0NC01NTU1NTU1NTU1NTX/"
        assertEquals(expectedBase64, base64)
    }

    @Test
    fun `encode BleOptions to expected json structure`() {
        val mapper = testMapper()
        val cborBytes = mapper.writeValueAsBytes(bleOptions)
        val actualNode = mapper.readTree(cborBytes)

        val jsonNodeFactory = JsonNodeFactory.instance
        val expectedOptions = jsonNodeFactory.objectNode()
            .put("0", true)
            .put("1", false)
            .put("10", uuidBytes)

        assertEquals(
            expectedOptions,
            actualNode,
            "CBOR structure should match expected JSON"
        )

        val json = JsonMapper.builder().build()
        val pretty = json.writerWithDefaultPrettyPrinter().writeValueAsString(actualNode)
        println(pretty)
    }
}
