package uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.json.JsonMapper
import tools.jackson.databind.module.SimpleModule
import tools.jackson.dataformat.cbor.CBORFactory
import tools.jackson.dataformat.cbor.CBORMapper
import tools.jackson.module.kotlin.KotlinModule
import uk.gov.onelogin.sharing.models.BleRetrievalStub.BLE_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.models.BleRetrievalStub.BLE_RETRIEVAL_METHOD_SERVER_MODE
import uk.gov.onelogin.sharing.models.DeviceEngagementStub.deviceRetrievalNodes
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCbor
import uk.gov.onelogin.sharing.models.mdoc.cbor.EmbeddedCborSerializer

class BleDeviceRetrievalMethodTest {
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
        val encoded = testMapper().writeValueAsBytes(BLE_RETRIEVAL_METHOD_SERVER_MODE)
        val base64 = Base64.getEncoder().encodeToString(encoded)

        assertEquals(BLE_EXPECTED_BASE_64, base64)
    }

    @Test
    fun `encode BleDeviceRetrievalMethod to expected json structure`() {
        val mapper = testMapper()
        val cborBytes = mapper.writeValueAsBytes(BLE_RETRIEVAL_METHOD_SERVER_MODE)
        val actualNode = mapper.readTree(cborBytes)

        val expectedNodes = deviceRetrievalNodes()

        assertEquals(
            "CBOR structure should match expected JSON",
            expectedNodes,
            actualNode
        )

        val json = JsonMapper.builder().build()
        val pretty = json.writerWithDefaultPrettyPrinter().writeValueAsString(actualNode)
        println(pretty)
    }
}
