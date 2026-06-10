package uk.gov.onelogin.sharing.cryptoService.deviceretrievalmethods

import java.util.Base64
import junit.framework.TestCase.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.BLE_EXPECTED_BASE_64
import uk.gov.onelogin.sharing.cryptoService.BleRetrievalStub.BLE_RETRIEVAL_METHOD_SERVER_MODE
import uk.gov.onelogin.sharing.cryptoService.DeviceEngagementStub.deviceRetrievalNodes
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.deviceretrievalmethods.toDto

class BleDeviceRetrievalMethodTest {

    @Test
    fun `encode BleDeviceRetrievalMethod to expected base64 string`() {
        val encoded = CborMapper.default.writeValueAsBytes(BLE_RETRIEVAL_METHOD_SERVER_MODE.toDto())
        val base64 = Base64.getEncoder().encodeToString(encoded)
        assertEquals(BLE_EXPECTED_BASE_64, base64)
    }

    @Test
    fun `encode BleDeviceRetrievalMethod to expected json structure`() {
        val cborBytes = CborMapper.default.writeValueAsBytes(
            BLE_RETRIEVAL_METHOD_SERVER_MODE.toDto()
        )
        val actualNode = CborMapper.default.readTree(cborBytes)
        assertEquals(
            "CBOR structure should match expected JSON",
            deviceRetrievalNodes(),
            actualNode
        )
    }

    // ISO 18013-5: DeviceRetrievalMethod array must use definite-length encoding
    @Test
    fun `DeviceRetrievalMethod encodes to definite-length array`() {
        val encoded = CborMapper.default.writeValueAsBytes(BLE_RETRIEVAL_METHOD_SERVER_MODE.toDto())
        assertEquals(0x83.toByte(), encoded[0])
    }
}
