package uk.gov.onelogin.sharing.cryptoService.deviceretrievalmethods

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.BLE_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.BLE_RETRIEVAL_METHOD_SERVER_MODE
import uk.gov.onelogin.sharing.cryptoService.DeviceEngagementStub.deviceRetrievalNodes
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.BleOptionsSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.DeviceRetrievalMethodSerializer
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.cryptoService.cbor.serializers.EmbeddedCborSerializer
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleDeviceRetrievalMethod
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions

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
    }

    // ISO 18013-5: DeviceRetrievalMethod array must use definite-length encoding
    @Test
    fun `DeviceRetrievalMethod encodes to definite-length array`() {
        val encoded = testMapper().writeValueAsBytes(BLE_RETRIEVAL_METHOD_SERVER_MODE)

        // 83 = definite-length array with 3 elements
        assertEquals(0x83.toByte(), encoded[0])
    }
}
