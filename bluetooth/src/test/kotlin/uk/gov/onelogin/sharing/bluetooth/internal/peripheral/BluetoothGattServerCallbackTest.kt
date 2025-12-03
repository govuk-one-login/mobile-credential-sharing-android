package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.peripheral.FakeGattEventEmitter
import uk.gov.onelogin.sharing.bluetooth.permissions.StubDeviceAddress.DEVICE_ADDRESS

class BluetoothGattServerCallbackTest {
    private val fakeEmitter = FakeGattEventEmitter()
    private val callback = GattServerCallback(fakeEmitter)
    private val device = mockk<BluetoothDevice>()
    private val service = mockk<BluetoothGattService>()

    @Before
    fun setup() {
        fakeEmitter.events.clear()
        every { device.address } returns DEVICE_ADDRESS
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
        assertEquals(DEVICE_ADDRESS, event.device.address)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
        assertEquals(BluetoothProfile.STATE_CONNECTED, event.newState)
    }

    @Test
    fun `onServiceAdded should emit service added event`() {
        val uuid = UUID.randomUUID()
        every { service.uuid } returns uuid

        callback.onServiceAdded(
            status = BluetoothGatt.GATT_SUCCESS,
            service = service
        )

        assertEquals(1, fakeEmitter.events.size)
        val event = fakeEmitter.events.single() as GattEvent.ServiceAdded
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
        assertEquals(uuid, event.service?.uuid)
    }
}
