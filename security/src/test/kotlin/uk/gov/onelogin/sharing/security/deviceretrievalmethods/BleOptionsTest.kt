package uk.gov.onelogin.sharing.security.deviceretrievalmethods

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.BleOptions
import uk.gov.onelogin.sharing.security.BleRetrievalStub.BLE_OPTIONS
import uk.gov.onelogin.sharing.security.BleRetrievalStub.BLE_OPTIONS_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.security.BleRetrievalStub.bleOptionNodes
import uk.gov.onelogin.sharing.security.cbor.serializers.BleOptionsSerializer
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCbor
import uk.gov.onelogin.sharing.security.cbor.serializers.EmbeddedCborSerializer

class BleOptionsTest {

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
        val encoded = testMapper().writeValueAsBytes(BLE_OPTIONS)
        val base64 = Base64.getEncoder().encodeToString(encoded)
        assertEquals(BLE_OPTIONS_EXPECTED_BASE_64, base64)
    }

    @Test
    fun `encode BleOptions to expected json structure`() {
        val mapper = testMapper()
        val cborBytes = mapper.writeValueAsBytes(BLE_OPTIONS)
        val actualNode = mapper.readTree(cborBytes)

        val expectedNodes = bleOptionNodes()

        assertEquals(
            "CBOR structure should match expected JSON",
            expectedNodes,
            actualNode
        )
    }
}
