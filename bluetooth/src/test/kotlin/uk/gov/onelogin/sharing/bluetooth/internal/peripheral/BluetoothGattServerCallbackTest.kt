package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.core.GattUuids
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestMessage
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestStart

class BluetoothGattServerCallbackTest {
    private val fakeEmitter = FakeGattServerEventEmitter()
    private val callback = GattServerCallback(
        gatGattEventEmitter = fakeEmitter,
        logger = MemorisedLogger()
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
            newState = android.bluetooth.BluetoothProfile.STATE_CONNECTED
        )

        assertEquals(1, fakeEmitter.events.size)
        val event = fakeEmitter.events.single() as GattServerCallbackEvent.ConnectionStateChange
        assertEquals(DEVICE_ADDRESS, event.device.address)
        assertEquals(BluetoothGatt.GATT_SUCCESS, event.status)
        assertEquals(
            android.bluetooth.BluetoothProfile.STATE_CONNECTED,
            event.newState
        )
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
        assertEquals(uuid, event.service.uuid)
    }

    @Test
    fun `onCharacteristicWriteRequest forwards STATE write as raw event`() {
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
        val event =
            fakeEmitter.events.single() as GattServerCallbackEvent.CharacteristicWriteRequest
        assertEquals(GattUuids.STATE_UUID, event.characteristicUuid)
        assertEquals(MdocState.START.code, event.value.single())
    }

    @Test
    fun `onCharacteristicWriteRequest forwards data write as raw event`() {
        val dataUuid = GattUuids.CLIENT_2_SERVER_UUID
        writeRequestMessage(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns dataUuid
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
            GattServerCallbackEvent.CharacteristicWriteRequest(
                device,
                dataUuid,
                FINAL_PART_MESSAGE
            ),
            fakeEmitter.events.single()
        )
    }

    companion object {
        val FINAL_PART_MESSAGE = byteArrayOf(0x00, 0x33, 0x44, 0x55)
    }
}
