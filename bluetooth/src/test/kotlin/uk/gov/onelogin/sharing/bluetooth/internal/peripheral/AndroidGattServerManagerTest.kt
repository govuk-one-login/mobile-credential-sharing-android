package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.MdocError
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceDefinition

class AndroidGattServerManagerTest {
    private val context = mockk<Context>(relaxed = true)
    private val bluetoothManager = mockk<BluetoothManager>(relaxed = true)
    private val gattServer = mockk<BluetoothGattServer>(relaxed = true)
    private val fakeGattService = AndroidGattServiceBuilder.build(
        GattServiceDefinition(
            UUID.randomUUID(),
            listOf()
        )
    )

    private lateinit var manager: AndroidGattServerManager

    @Before
    fun setup() {
        manager = AndroidGattServerManager(
            context = context,
            bluetoothManager = bluetoothManager,
            gattService = fakeGattService
        )
    }

    @Test
    fun `gatt server starts successfully`() {
        every {
            bluetoothManager.openGattServer(context, any())
        } returns gattServer

        manager.open()

        verify(exactly = 1) { gattServer.clearServices() }
        verify(exactly = 1) { gattServer.addService(fakeGattService) }
    }

    @Test
    fun `emits error when gatt server is not available`() = runTest {
        every {
            bluetoothManager.openGattServer(context, any())
        } returns null

        manager.events.test {
            manager.open()

            val event = awaitItem()
            assert(event is GattServerEvent.Error)
            assertEquals(
                GattServerEvent.Error(MdocError.GATT_NOT_AVAILABLE),
                event
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `gatt server starts and closes successfully`() {
        every {
            bluetoothManager.openGattServer(context, any())
        } returns gattServer

        manager.open()
        manager.close()

        verify(exactly = 1) { gattServer.clearServices() }
        verify(exactly = 1) { gattServer.addService(fakeGattService) }
        verify(exactly = 1) { gattServer.close() }
    }

    @Test
    fun `emits Connected event when device connects successfully`() = runTest {
        val callbackSlot = slot<BluetoothGattServerCallback>()
        every {
            bluetoothManager.openGattServer(context, capture(callbackSlot))
        } returns gattServer

        manager.open()

        val device = mockk<BluetoothDevice>()
        every { device.address } returns "AA:BB:CC:DD:EE:FF"

        manager.events.test {
            callbackSlot.captured.onConnectionStateChange(
                device,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTED
            )

            assertEquals(
                GattServerEvent.Connected("AA:BB:CC:DD:EE:FF"),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Disconnected after a successful connect`() = runTest {
        val callbackSlot = slot<BluetoothGattServerCallback>()
        every {
            bluetoothManager.openGattServer(context, capture(callbackSlot))
        } returns gattServer

        manager.open()

        val device = mockk<BluetoothDevice>()
        every { device.address } returns "AA:BB:CC:DD:EE:FF"

        manager.events.test {
            callbackSlot.captured.onConnectionStateChange(
                device,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTED
            )

            assertEquals(
                GattServerEvent.Connected("AA:BB:CC:DD:EE:FF"),
                awaitItem()
            )

            // disconnect
            callbackSlot.captured.onConnectionStateChange(
                device,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_DISCONNECTED
            )

            assertEquals(
                GattServerEvent.Disconnected("AA:BB:CC:DD:EE:FF"),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits UnsupportedEvent for unhandled events`() = runTest {
        val callbackSlot = slot<BluetoothGattServerCallback>()
        every {
            bluetoothManager.openGattServer(context, capture(callbackSlot))
        } returns gattServer

        manager.open()

        val device = mockk<BluetoothDevice>()
        every { device.address } returns "AA:BB:CC:DD:EE:FF"

        manager.events.test {
            callbackSlot.captured.onConnectionStateChange(
                device,
                BluetoothGatt.GATT_FAILURE,
                BluetoothProfile.STATE_CONNECTED
            )

            assertEquals(
                GattServerEvent.UnsupportedEvent(
                    address = "AA:BB:CC:DD:EE:FF",
                    status = BluetoothGatt.GATT_FAILURE,
                    newState = BluetoothProfile.STATE_CONNECTED
                ),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }
}
