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
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.FakeGattEventEmitter
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestEmptyValue
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestMessage
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestStart
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestUnknown

class BluetoothGattServerCallbackTest {
    private val fakeEmitter = FakeGattEventEmitter()
    private val callback = GattServerCallback(
        gatGattEventEmitter = fakeEmitter,
        logger = SystemLogger()
    )
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
    fun `onCharacteristicWriteRequest empty opcode should not emit event`() {
        val args = writeRequestEmptyValue()

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
    fun `emits event when receiving single-part message`() {
        val args = writeRequestMessage(message = FINAL_PART_MESSAGE)

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
            GattEvent.MessageReceived(byteArrayOf(0x33, 0x44, 0x55)),
            fakeEmitter.events.firstOrNull()
        )
    }

    @Test
    fun `emits no event when receiving first part of single-part message`() {
        val args = writeRequestMessage(message = NON_FINAL_PART_MESSAGE)

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
    fun `emits event containing whole message when receiving a multi-part message`() {
        val firstPart = writeRequestMessage(message = NON_FINAL_PART_MESSAGE)

        callback.onCharacteristicWriteRequest(
            device = firstPart.device,
            requestId = firstPart.requestId,
            characteristic = firstPart.characteristic,
            preparedWrite = firstPart.preparedWrite,
            responseNeeded = firstPart.responseNeeded,
            offset = firstPart.offset,
            value = firstPart.value
        )
        assertEquals(0, fakeEmitter.events.size)

        val secondPart = writeRequestMessage(message = FINAL_PART_MESSAGE)

        callback.onCharacteristicWriteRequest(
            device = secondPart.device,
            requestId = secondPart.requestId,
            characteristic = secondPart.characteristic,
            preparedWrite = secondPart.preparedWrite,
            responseNeeded = secondPart.responseNeeded,
            offset = secondPart.offset,
            value = secondPart.value
        )

        assertEquals(1, fakeEmitter.events.size)
        assertEquals(
            GattEvent.MessageReceived(byteArrayOf(0x11, 0x22, 0x33, 0x44, 0x55)),
            fakeEmitter.events.firstOrNull()
        )
    }

    companion object {
        val NON_FINAL_PART_MESSAGE = byteArrayOf(0x01, 0x11, 0x22)
        val FINAL_PART_MESSAGE = byteArrayOf(0x00, 0x33, 0x44, 0x55)
    }
}
