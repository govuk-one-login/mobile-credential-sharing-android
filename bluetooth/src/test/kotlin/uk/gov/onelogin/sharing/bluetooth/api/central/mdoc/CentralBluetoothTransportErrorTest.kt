package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError

class CentralBluetoothTransportErrorTest {

    @Test
    fun `BLUETOOTH_PERMISSION_MISSING maps correctly`() {
        assertEquals(
            CentralBluetoothTransportError.BLUETOOTH_PERMISSION_MISSING,
            CentralBluetoothTransportError.fromClientError(
                ClientError.BLUETOOTH_PERMISSION_MISSING
            )
        )
    }

    @Test
    fun `BLUETOOTH_GATT_NOT_AVAILABLE maps correctly`() {
        assertEquals(
            CentralBluetoothTransportError.GATT_NOT_AVAILABLE,
            CentralBluetoothTransportError.fromClientError(
                ClientError.BLUETOOTH_GATT_NOT_AVAILABLE
            )
        )
    }

    @Test
    fun `SERVICE_NOT_FOUND maps correctly`() {
        assertEquals(
            CentralBluetoothTransportError.SERVICE_NOT_FOUND,
            CentralBluetoothTransportError.fromClientError(ClientError.SERVICE_NOT_FOUND)
        )
    }

    @Test
    fun `INVALID_SERVICE maps correctly`() {
        assertEquals(
            CentralBluetoothTransportError.INVALID_SERVICE,
            CentralBluetoothTransportError.fromClientError(ClientError.INVALID_SERVICE)
        )
    }

    @Test
    fun `FAILED_TO_SUBSCRIBE maps correctly`() {
        assertEquals(
            CentralBluetoothTransportError.FAILED_TO_SUBSCRIBE,
            CentralBluetoothTransportError.fromClientError(ClientError.FAILED_TO_SUBSCRIBE)
        )
    }

    @Test
    fun `FAILED_TO_START maps correctly`() {
        assertEquals(
            CentralBluetoothTransportError.FAILED_TO_START,
            CentralBluetoothTransportError.fromClientError(ClientError.FAILED_TO_START)
        )
    }

    @Test
    fun `SERVICE_DISCOVERED_ERROR maps to INVALID_SERVICE`() {
        assertEquals(
            CentralBluetoothTransportError.INVALID_SERVICE,
            CentralBluetoothTransportError.fromClientError(
                ClientError.SERVICE_DISCOVERED_ERROR
            )
        )
    }
}
