package uk.gov.onelogin.sharing.cryptoService.deviceretrievalmethods

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.BLE_OPTIONS
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.BLE_OPTIONS_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.D_3_1_BLE_OPTIONS
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.D_3_1_BLE_OPTIONS_HEX
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.bleOptionNodes
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toDto

class BleOptionsTest {

    @Test
    fun `encode BleOptions to expected base64 string`() {
        val encoded = CborMapper.default.writeValueAsBytes(BLE_OPTIONS.toDto())
        val base64 = Base64.getEncoder().encodeToString(encoded)
        assertEquals(BLE_OPTIONS_EXPECTED_BASE_64, base64)
    }

    @Test
    fun `encode BleOptions to expected json structure`() {
        val cborBytes = CborMapper.default.writeValueAsBytes(BLE_OPTIONS.toDto())
        val actualNode = CborMapper.default.readTree(cborBytes)
        assertEquals("CBOR structure should match expected JSON", bleOptionNodes(), actualNode)
    }

    // Expected CBOR structure derived from ISO 18013-5 Appendix D.3.1
    @Test
    fun `BleOptions encodes to definite-length map matching D_3_1`() {
        val encoded = CborMapper.default.writeValueAsBytes(D_3_1_BLE_OPTIONS.toDto())
        assertEquals(D_3_1_BLE_OPTIONS_HEX, encoded.toHexString())
    }
}
