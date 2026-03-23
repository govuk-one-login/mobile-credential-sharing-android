package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues

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
        assertEquals(DEVICE_ADDRESS, event.gatt.device.address)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
    }

    @Test
    fun `onMtuChanged emits MtuChange event`() {
        callback.onMtuChanged(
            gatt,
            MtuValues.MAX_MTU,
            BluetoothGatt.GATT_SUCCESS
        )

        assertEquals(1, fakeEmitter.events.size)

        val event = fakeEmitter.events.single() as GattEvent.MtuChange
        assertEquals(DEVICE_ADDRESS, event.gatt.device.address)
        assertEquals(MtuValues.MAX_MTU, event.mtu)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
    }

    @Test
    fun `onCharacteristicWrite emits CharacteristicWrite event`() {
        val characteristic = mockk<BluetoothGattCharacteristic>()

        callback.onCharacteristicWrite(
            gatt,
            characteristic,
            BluetoothGatt.GATT_SUCCESS
        )

        assertEquals(1, fakeEmitter.events.size)

        val event = fakeEmitter.events.single() as GattEvent.CharacteristicWrite
        assertEquals(DEVICE_ADDRESS, event.gatt.device.address)
        assertEquals(characteristic, event.characteristic)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
    }

    @Test
    fun `onDescriptorWrite emits DescriptorWrite event`() {
        val descriptor = mockk<BluetoothGattDescriptor>()

        callback.onDescriptorWrite(
            gatt,
            descriptor,
            BluetoothGatt.GATT_SUCCESS
        )

        assertEquals(1, fakeEmitter.events.size)

        val event = fakeEmitter.events.single() as GattEvent.DescriptorWrite
        assertEquals(DEVICE_ADDRESS, event.gatt.device.address)
        assertEquals(descriptor, event.descriptor)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
    }
}
