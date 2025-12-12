package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent

private const val ADDRESS = "AA:BB:CC:DD:EE:FF"

class ConnectionStateChangeMappingTest {
    private val device = mockk<BluetoothDevice> {
        every { address } returns ADDRESS
    }

    @Test
    fun `maps successful connect to Connected`() {
        val event = GattEvent.ConnectionStateChange(
            device = device,
            status = BluetoothGatt.GATT_SUCCESS,
            newState = BluetoothProfile.STATE_CONNECTED
        )

        val result = event.toGattServerEvent()

        assertEquals(
            GattServerEvent.Connected(ADDRESS),
            result
        )
    }

    @Test
    fun `maps disconnected to Disconnected`() {
        val event = GattEvent.ConnectionStateChange(
            device = device,
            status = BluetoothGatt.GATT_SUCCESS,
            newState = BluetoothProfile.STATE_DISCONNECTED
        )

        val result = event.toGattServerEvent()

        assertEquals(
            GattServerEvent.Disconnected(ADDRESS),
            result
        )
    }

    @Test
    fun `maps unexpected codes to UnsupportedEvent`() {
        val event = GattEvent.ConnectionStateChange(
            device = device,
            status = 42,
            newState = 123
        )

        val result = event.toGattServerEvent()

        assertEquals(
            GattServerEvent.UnsupportedEvent(
                address = ADDRESS,
                status = 42,
                newState = 123
            ),
            result
        )
    }
}
