package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyCount
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.internal.validator.FakeServiceValidator
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState
import uk.gov.onelogin.sharing.bluetooth.permissions.FakePermissionChecker

@RunWith(RobolectricTestRunner::class)
internal class AndroidGattClientManagerTest {
    private val context = mockk<Context>(relaxed = true)
    private val bluetoothDevice = mockk<BluetoothDevice>(relaxed = true)
    private val bluetoothGatt = mockk<BluetoothGatt>(relaxed = true)
    private val fakePermissionChecker = FakePermissionChecker()

    private val fakeServiceValidator = FakeServiceValidator()
    private val logger = SystemLogger()
    private val uuid = UUID.randomUUID()

    private lateinit var manager: AndroidGattClientManager

    private var originalSdkInt: Int = Build.VERSION.SDK_INT

    @Before
    fun setup() {
        originalSdkInt = Build.VERSION.SDK_INT
        mockkStatic("androidx.core.content.ContextCompat")

        manager = AndroidGattClientManager(
            context,
            fakePermissionChecker,
            fakeServiceValidator,
            logger
        )
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.core.content.ContextCompat")
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            originalSdkInt
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
    fun `emits error when discovered service is not valid`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()
        fakeServiceValidator.errors = mutableListOf("error")

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
                GattClientEvent.Error(
                    ClientError.INVALID_SERVICE
                ),
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
                GattClientEvent.ServicesDiscovered,
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
    fun `requests 512 max transmission unit when service discovery is successful`() = runTest {
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
                BluetoothGatt.GATT_SUCCESS
            )

            skipItems(1)

            verify { bluetoothGatt.requestMtu(MtuValues.MAX_POSSIBLE) }
        }
    }

    @Test
    fun `subscribes to state changes when service discovery is successful`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()

        every {
            bluetoothDevice.connectGatt(
                context,
                any(),
                capture(callbackSlot),
                any()
            )
        } returns bluetoothGatt

        val service = mockk<BluetoothGattService>(relaxed = true)
        every { bluetoothGatt.getService(any()) } returns service

        val stateCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateCharacteristic

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

            skipItems(1)

            verify {
                bluetoothGatt.setCharacteristicNotification(
                    stateCharacteristic,
                    true
                )
            }
        }
    }

    @Test
    fun `subscribes to serverToClient messages when service discovery is successful`() = runTest {
        val callbackSlot = slot<BluetoothGattCallback>()

        every {
            bluetoothDevice.connectGatt(
                context,
                any(),
                capture(callbackSlot),
                any()
            )
        } returns bluetoothGatt

        val service = mockk<BluetoothGattService>(relaxed = true)
        every { bluetoothGatt.getService(any()) } returns service

        val serverToClientCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every {
            service.getCharacteristic(GattUuids.STATE_UUID)
        } returns serverToClientCharacteristic

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

            skipItems(1)

            verify {
                bluetoothGatt.setCharacteristicNotification(
                    serverToClientCharacteristic,
                    true
                )
            }
        }
    }

    @Test
    fun `sets state to start when service discovery is successful - modern API`() = runTest {
        ReflectionHelpers.setStaticField(
            Build.VERSION::class.java,
            "SDK_INT",
            Build.VERSION_CODES.TIRAMISU
        )

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
                BluetoothGatt.GATT_SUCCESS
            )

            skipItems(1)

            verify {
                bluetoothGatt.writeCharacteristic(
                    any(),
                    byteArrayOf(MdocState.START.code),
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `sets state to start when service discovery is successful - deprecated API version`() =
        runTest {
            ReflectionHelpers.setStaticField(
                Build.VERSION::class.java,
                "SDK_INT",
                Build.VERSION_CODES.S
            )

            val callbackSlot = slot<BluetoothGattCallback>()

            every {
                bluetoothDevice.connectGatt(
                    context,
                    any(),
                    capture(callbackSlot),
                    any()
                )
            } returns bluetoothGatt

            val service = mockk<BluetoothGattService>(relaxed = true)
            every { bluetoothGatt.getService(any()) } returns service

            val stateCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
            every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateCharacteristic

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

                skipItems(1)

                verify { stateCharacteristic.setValue(byteArrayOf(MdocState.START.code)) }
                verify { bluetoothGatt.writeCharacteristic(stateCharacteristic) }
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
