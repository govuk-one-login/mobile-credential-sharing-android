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
import uk.gov.onelogin.sharing.bluetooth.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestNullValue
import uk.gov.onelogin.sharing.bluetooth.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestStart
import uk.gov.onelogin.sharing.bluetooth.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestUnknown

class BluetoothGattServerCallbackTest {
    private val fakeEmitter = FakeGattEventEmitter()
    private val callback = GattServerCallback(fakeEmitter)
    private val device = mockk<BluetoothDevice>()
    private val service = mockk<BluetoothGattService>()

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

    @Test
    fun `onCharacteristicWriteRequest 0x01 should emit ConnectionStateStarted event`() {
        val args = writeRequestStart()

        callback.onCharacteristicWriteRequest(
            device = args.device,
            requestId = args.requestId,
            characteristic = args.characteristic,
            preparedWrite = args.preparedWrite,
            responseNeeded = args.responseNeeded,
            offset = args.offset,
            value = args.value
        )

        assertEquals(1, fakeEmitter.events.size)
        assertEquals(
            GattEvent.ConnectionStateStarted,
            fakeEmitter.events.single()
        )
    }

    @Test
    fun `onCharacteristicWriteRequest unknown opcode should not emit event`() {
        val args = writeRequestUnknown()

        callback.onCharacteristicWriteRequest(
            device = args.device,
            requestId = args.requestId,
            characteristic = args.characteristic,
            preparedWrite = args.preparedWrite,
            responseNeeded = args.responseNeeded,
            offset = args.offset,
            value = args.value
        )

        assertEquals(0, fakeEmitter.events.size)
    }

    @Test
    fun `onCharacteristicWriteRequest null opcode should not emit event`() {
        val args = writeRequestNullValue()

        callback.onCharacteristicWriteRequest(
            device = args.device,
            requestId = args.requestId,
            characteristic = args.characteristic,
            preparedWrite = args.preparedWrite,
            responseNeeded = args.responseNeeded,
            offset = args.offset,
            value = args.value
        )

        assertEquals(0, fakeEmitter.events.size)
    }
}
