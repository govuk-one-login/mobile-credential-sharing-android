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
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.FakeGattEventEmitter
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallbackEvent
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids
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
        val event = fakeEmitter.events.single() as GattServerCallbackEvent.ConnectionStateChange
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
        val event = fakeEmitter.events.single() as GattServerCallbackEvent.ServiceAdded
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
        assertEquals(uuid, event.service?.uuid)
    }

    @Test
    fun `onCharacteristicWriteRequest 0x01 should emit ConnectionStateStarted event`() {
        writeRequestStart(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns GattUuids.STATE_UUID
            }
        ).run {
            callback.onCharacteristicWriteRequest(
                device = device,
                requestId = requestId,
                characteristic = characteristic,
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                value = value
            )
        }

        assertEquals(1, fakeEmitter.events.size)
        assertEquals(
            GattServerCallbackEvent.ConnectionStateStarted,
            fakeEmitter.events.single()
        )
    }

    @Test
    fun `onCharacteristicWriteRequest unknown opcode should not emit event`() {
        writeRequestUnknown(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns UUID.randomUUID()
            }
        ).run {
            callback.onCharacteristicWriteRequest(
                device = device,
                requestId = requestId,
                characteristic = characteristic,
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                value = value
            )
        }

        assertEquals(0, fakeEmitter.events.size)
    }

    @Test
    fun `onCharacteristicWriteRequest empty opcode should not emit event`() {
        writeRequestEmptyValue(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns UUID.randomUUID()
            }
        ).run {
            callback.onCharacteristicWriteRequest(
                device = device,
                requestId = requestId,
                characteristic = characteristic,
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                value = value
            )
        }

        assertEquals(0, fakeEmitter.events.size)
    }

    @Test
    fun `emits event when receiving single-part message`() {
        writeRequestMessage(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns GattUuids.CLIENT_2_SERVER_UUID
            },
            message = FINAL_PART_MESSAGE
        ).run {
            callback.onCharacteristicWriteRequest(
                device = device,
                requestId = requestId,
                characteristic = characteristic,
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                value = value
            )
        }

        assertEquals(1, fakeEmitter.events.size)
        assertEquals(
            GattServerCallbackEvent.MessageReceived(byteArrayOf(0x33, 0x44, 0x55)),
            fakeEmitter.events.firstOrNull()
        )
    }

    @Test
    fun `emits no event when receiving first part of single-part message`() {
        writeRequestMessage(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns GattUuids.CLIENT_2_SERVER_UUID
            },
            message = NON_FINAL_PART_MESSAGE
        ).run {
            callback.onCharacteristicWriteRequest(
                device = device,
                requestId = requestId,
                characteristic = characteristic,
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                value = value
            )
        }

        assertEquals(0, fakeEmitter.events.size)
    }

    @Test
    fun `emits event containing whole message when receiving a multi-part message`() {
        writeRequestMessage(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns GattUuids.CLIENT_2_SERVER_UUID
            },
            message = NON_FINAL_PART_MESSAGE
        ).run {
            callback.onCharacteristicWriteRequest(
                device = device,
                requestId = requestId,
                characteristic = characteristic,
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                value = value
            )
        }
        assertEquals(0, fakeEmitter.events.size)

        writeRequestMessage(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns GattUuids.CLIENT_2_SERVER_UUID
            },
            message = FINAL_PART_MESSAGE
        ).run {
            callback.onCharacteristicWriteRequest(
                device = device,
                requestId = requestId,
                characteristic = characteristic,
                preparedWrite = preparedWrite,
                responseNeeded = responseNeeded,
                offset = offset,
                value = value
            )
        }

        assertEquals(1, fakeEmitter.events.size)
        assertEquals(
            GattServerCallbackEvent.MessageReceived(byteArrayOf(0x11, 0x22, 0x33, 0x44, 0x55)),
            fakeEmitter.events.firstOrNull()
        )
    }

    companion object {
        val NON_FINAL_PART_MESSAGE = byteArrayOf(0x01, 0x11, 0x22)
        val FINAL_PART_MESSAGE = byteArrayOf(0x00, 0x33, 0x44, 0x55)
    }
}
