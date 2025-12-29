package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyCount
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.internal.central.AndroidGattClientManager
import uk.gov.onelogin.sharing.bluetooth.permissions.FakePermissionChecker

internal class AndroidGattClientManagerTest {
    private val context = mockk<Context>(relaxed = true)
    private val bluetoothDevice = mockk<BluetoothDevice>(relaxed = true)
    private val bluetoothGatt = mockk<BluetoothGatt>(relaxed = true)
    private val fakePermissionChecker = FakePermissionChecker()
    private val logger = SystemLogger()
    private val uuid = UUID.randomUUID()

    private lateinit var manager: AndroidGattClientManager

    @Before
    fun setup() {
        manager = AndroidGattClientManager(
            context,
            fakePermissionChecker,
            logger
        )
    }

    @Test
    fun `returns error if permission is not granted`() = runTest {
        fakePermissionChecker.hasCentralPermissions = false

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            assertEquals(
                GattClientEvent.Error(
                    ClientError.BLUETOOTH_PERMISSION_MISSING
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `returns error if bluetooth gatt is null`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()

        every {
            bluetoothDevice.connectGatt(
                context,
                any(),
                capture(callbackSlot),
                any()
            )
        } returns null

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            skipItems(1)

            assertEquals(
                GattClientEvent.Error(
                    ClientError.BLUETOOTH_GATT_NOT_AVAILABLE
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `returns error if exception is thrown`() = runTest {
        every {
            bluetoothDevice.connectGatt(
                any(),
                any(),
                any(),
                any()
            )
        } throws SecurityException()

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            assertEquals(
                GattClientEvent.Connecting,
                awaitItem()
            )

            assert(logger.contains("Security exception"))

            assertEquals(
                GattClientEvent.Error(
                    ClientError.BLUETOOTH_PERMISSION_MISSING
                ),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error when service discovery is not successful`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()

        every {
            bluetoothDevice.connectGatt(
                context,
                any(),
                capture(callbackSlot),
                any()
            )
        } returns bluetoothGatt

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            skipItems(1)

            callbackSlot.captured.onServicesDiscovered(
                bluetoothGatt,
                BluetoothGatt.GATT_FAILURE
            )

            assertEquals(
                GattClientEvent.Error(ClientError.SERVICE_DISCOVERED_ERROR),
                awaitItem()
            )
        }
    }

    @Test
    fun `emits service not found when get service discovery is not successful`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()

        every {
            bluetoothDevice.connectGatt(
                context,
                any(),
                capture(callbackSlot),
                any()
            )
        } returns bluetoothGatt

        every { bluetoothGatt.getService(any()) } returns null

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            skipItems(1)

            callbackSlot.captured.onServicesDiscovered(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS
            )

            assertEquals(
                GattClientEvent.Error(ClientError.SERVICE_NOT_FOUND),
                awaitItem()
            )
        }
    }

    @Test
    fun `emits service connected when get service discovery is successful`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()

        every {
            bluetoothDevice.connectGatt(
                context,
                any(),
                capture(callbackSlot),
                any()
            )
        } returns bluetoothGatt

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            assertEquals(
                GattClientEvent.Connecting,
                awaitItem()
            )

            callbackSlot.captured.onServicesDiscovered(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS
            )

            assertEquals(
                GattClientEvent.ServicesDiscovered(bluetoothGatt.getService(uuid)),
                awaitItem()
            )

            callbackSlot.captured.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothGatt.STATE_CONNECTED
            )

            verify { bluetoothGatt.discoverServices() }

            assertEquals(
                GattClientEvent.Connected(bluetoothGatt.device.address),
                awaitItem()
            )
        }
    }

    @Test
    fun `emits service disconnected`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()

        every {
            bluetoothDevice.connectGatt(
                context,
                any(),
                capture(callbackSlot),
                any()
            )
        } returns bluetoothGatt

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            skipItems(1)

            callbackSlot.captured.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothGatt.STATE_DISCONNECTED
            )

            assertEquals(
                GattClientEvent.Disconnected(bluetoothGatt.device.address),
                awaitItem()
            )
        }
    }

    @Test
    fun `emits unsupported event`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()

        every {
            bluetoothDevice.connectGatt(
                context,
                any(),
                capture(callbackSlot),
                any()
            )
        } returns bluetoothGatt

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            skipItems(1)

            callbackSlot.captured.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothGatt.STATE_DISCONNECTING
            )

            assertEquals(
                GattClientEvent.UnsupportedEvent(
                    bluetoothGatt.device.address,
                    BluetoothGatt.GATT_SUCCESS,
                    BluetoothGatt.STATE_DISCONNECTING
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `disconnect calls bluetoothGatt disconnect`() {
        manager.disconnect()

        verifyCount { bluetoothGatt.disconnect() }
        verifyCount { bluetoothGatt.close() }
    }
}
