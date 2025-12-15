package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothGatt
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.client.FakeClientEventEmitter
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattClientCallback
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattEvent

class GattClientCallbackTest {
    private val fakeEmitter = FakeClientEventEmitter()
    private val callback = GattClientCallback(fakeEmitter)
    private val gatt = mockk<BluetoothGatt>()

    @Before
    fun setup() {
        fakeEmitter.events.clear()
        every { gatt.device.address } returns DEVICE_ADDRESS
    }

    @Test
    fun `onConnectionSateChange emits connection state change event`() {
        callback.onConnectionStateChange(
            gatt,
            BluetoothGatt.GATT_SUCCESS,
            BluetoothGatt.STATE_CONNECTED
        )

        assertEquals(1, fakeEmitter.events.size)

        val event = fakeEmitter.events.single() as GattEvent.ConnectionStateChange
        assertEquals(DEVICE_ADDRESS, event.gatt.device.address)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
        assertEquals(BluetoothGatt.STATE_CONNECTED, event.newState)
    }

    @Test
    fun `onServicesDiscovered emits services discovered event`() {
        callback.onServicesDiscovered(
            gatt,
            BluetoothGatt.GATT_SUCCESS
        )

        assertEquals(1, fakeEmitter.events.size)

        val event = fakeEmitter.events.single() as GattEvent.ServicesDiscovered
        assertEquals(DEVICE_ADDRESS, event.bluetoothGatt.device.address)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
    }
}
