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
import uk.gov.onelogin.sharing.models.mdoc.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod.Companion.BLE_TYPE
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.DeviceRetrievalMethod.Companion.BLE_VERSION

class BleDeviceRetrievalMethodTest {
    private val uuidBytes = "11111111-2222-3333-4444-555555555555".toByteArray()
    private val bleDeviceRetrievalMethod =
        BleDeviceRetrievalMethod(
            type = BLE_TYPE,
            version = BLE_VERSION,
            options = BleOptions(
                serverMode = true,
                clientMode = false,
                peripheralServerModeUuid = EmbeddedCbor(uuidBytes)
            )
        )

    private fun testMapper(): ObjectMapper = CBORMapper.builder(CBORFactory())
        .addModule(KotlinModule.Builder().build())
        .addModule(
            SimpleModule().apply {
                addSerializer(EmbeddedCbor::class.java, EmbeddedCborSerializer())
                addSerializer(
                    BleOptions::class.java,
                    BleOptionsSerializer()
                )
                addSerializer(
                    BleDeviceRetrievalMethod::class.java,
                    DeviceRetrievalMethodSerializer()
                )
            }
        )
        .build()

    @Test
    fun `encode BleDeviceRetrievalMethod to expected base64 string`() {
        val encoded = testMapper().writeValueAsBytes(bleDeviceRetrievalMethod)
        val base64 = Base64.getEncoder().encodeToString(encoded)

        val expectedBase64 =
            "nwIBv2Ew9WEx9GIxMNgYWCQxMTExMTExMS0yMjIyLTMzMzMtNDQ0NC01NTU1NTU1NTU1NTX//w=="
        assertEquals(expectedBase64, base64)
    }

    @Test
    fun `encode BleDeviceRetrievalMethod to expected json structure`() {
        val mapper = testMapper()
        val cborBytes = mapper.writeValueAsBytes(bleDeviceRetrievalMethod)
        val actualNode = mapper.readTree(cborBytes)

        val jsonNodeFactory = JsonNodeFactory.instance
        val options = jsonNodeFactory.objectNode()
            .put("0", true)
            .put("1", false)
            .put("10", uuidBytes)

        val expectedNode = jsonNodeFactory.arrayNode()
            .add(BLE_TYPE)
            .add(BLE_VERSION)
            .add(options)

        assertEquals(
            expectedNode,
            actualNode,
            "CBOR structure should match expected JSON"
        )

        val json = JsonMapper.builder().build()
        val pretty = json.writerWithDefaultPrettyPrinter().writeValueAsString(actualNode)
        println(pretty)
    }
}
