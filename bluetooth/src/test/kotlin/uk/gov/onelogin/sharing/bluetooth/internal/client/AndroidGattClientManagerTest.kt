package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.client.FakeServiceValidator
import uk.gov.onelogin.sharing.bluetooth.permissions.FakePermissionChecker
import java.util.UUID
import kotlin.test.assertEquals

internal class AndroidGattClientManagerTest {
    private val context = mockk<Context>(relaxed = true)
    private val bluetoothDevice = mockk<BluetoothDevice>(relaxed = true)
    private val bluetoothGatt = mockk<BluetoothGatt>(relaxed = true)
    private val fakePermissionChecker = FakePermissionChecker()
    private val serviceValidator = FakeServiceValidator()
    private val logger = SystemLogger()
    private val uuid = UUID.randomUUID()

    private lateinit var manager: AndroidGattClientManager

    @Before
    fun setup() {
        manager = AndroidGattClientManager(
            context,
            fakePermissionChecker,
            serviceValidator,
            logger
        )
    }

    @Test
    fun `returns error if permission is not granted`() = runTest {
        fakePermissionChecker.hasPermission = false

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
                    ClientError.BLUETOOTH_GATT_NOT_AVAILABLE
                ),
                awaitItem()
            )
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

}