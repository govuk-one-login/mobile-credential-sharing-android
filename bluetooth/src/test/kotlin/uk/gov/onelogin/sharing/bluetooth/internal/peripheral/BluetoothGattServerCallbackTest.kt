package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.peripheral.FakeGattEventEmitter

class BluetoothGattServerCallbackTest {
    private val fakeEmitter = FakeGattEventEmitter()
    private val callback = GattServerCallback(fakeEmitter)
    private val device = mockk<BluetoothDevice>()

    @Before
    fun setup() {
        fakeEmitter.events.clear()
        every { device.address } returns "device-address"
    }

    @Test
    fun `onConnectionStateChange should emit connected event`() {
        callback.onConnectionStateChange(
            device = device,
            status = BluetoothGatt.GATT_SUCCESS,
            newState = BluetoothProfile.STATE_CONNECTED
        )

        assertEquals(1, fakeEmitter.events.size)
        val event = fakeEmitter.events.single() as GattEvent.ConnectionStateChange
        assertEquals("device-address", event.device.address)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
        assertEquals(BluetoothProfile.STATE_CONNECTED, event.newState)
    }
}
