package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import app.cash.turbine.test
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.mockBluetoothDevice
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattServerMock.setupNullGattServer
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.GattServerMock.setupOpenGattServer
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.DescriptorWriteRequestStub.OnDescriptorWriteRequestArgs
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceDefinition
import uk.gov.onelogin.sharing.bluetooth.permissions.FakePermissionChecker

class AndroidGattServerManagerTest {
    private val context = mockk<Context>(relaxed = true)
    private val bluetoothManager = mockk<BluetoothManager>(relaxed = true)
    private val device = mockBluetoothDevice()
    private val fakeGattService = AndroidGattServiceBuilder.build(
        GattServiceDefinition(
            UUID.randomUUID(),
            listOf()
        )
    )
    private val fakePermissionChecker = FakePermissionChecker()

    private lateinit var manager: AndroidGattServerManager
    private val uuid = UUID.randomUUID()

    @Before
    fun setup() {
        manager = AndroidGattServerManager(
            context = context,
            bluetoothManager = bluetoothManager,
            gattServiceFactory = { fakeGattService },
            permissionsChecker = fakePermissionChecker,
            logger = SystemLogger()
        )
    }

    @Test
    fun `gatt server starts successfully`() {
        val (_, server) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        verify(exactly = 1) { server.clearServices() }
        verify(exactly = 1) { server.addService(fakeGattService) }
    }

    @Test
    fun `emits error when gatt server is not available`() = runTest {
        setupNullGattServer(bluetoothManager, context)

        manager.events.test {
            manager.open(uuid)

            val event = awaitItem()
            assert(event is GattServerEvent.Error)
            assertEquals(
                GattServerEvent.Error(GattServerError.GATT_NOT_AVAILABLE),
                event
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `gatt server starts and closes successfully`() = runTest {
        val (_, server) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        manager.events.test {
            manager.close()

            assertEquals(
                GattServerEvent.ServiceStopped,
                awaitItem()
            )
        }

        verify(exactly = 1) { server.clearServices() }
        verify(exactly = 1) { server.addService(fakeGattService) }
        verify(exactly = 1) { server.close() }
    }

    @Test
    fun `emits Connected event when device connects successfully`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)
        manager.open(uuid)

        manager.events.test {
            callbackSlot.captured.onConnectionStateChange(
                device,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTED
            )

            assertEquals(
                GattServerEvent.Connected(DEVICE_ADDRESS),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Disconnected after a successful connect`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)
        manager.open(uuid)

        manager.events.test {
            callbackSlot.captured.onConnectionStateChange(
                device,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTED
            )

            assertEquals(
                GattServerEvent.Connected(DEVICE_ADDRESS),
                awaitItem()
            )

            // disconnect
            callbackSlot.captured.onConnectionStateChange(
                device,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothProfile.STATE_DISCONNECTED
            )

            assertEquals(
                GattServerEvent.Disconnected(DEVICE_ADDRESS),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Service added event`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)
        val service = mockk<BluetoothGattService>()

        manager.open(uuid)

        manager.events.test {
            callbackSlot.captured.onServiceAdded(
                BluetoothGatt.GATT_SUCCESS,
                service
            )

            assertEquals(
                GattServerEvent.ServiceAdded(
                    status = BluetoothGatt.GATT_SUCCESS,
                    service = service
                ),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits session state started event`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        manager.events.test {
            CharacteristicWriteRequestStub.writeRequestStart().run {
                callbackSlot.captured.onCharacteristicWriteRequest(
                    device,
                    requestId,
                    characteristic,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    value
                )
            }

            assertEquals(
                GattServerEvent.SessionStarted,
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sends success response if response is needed`() {
        val (callbackSlot, gattServer) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        val args = OnDescriptorWriteRequestArgs()
        callbackSlot.captured.invokeDescriptorWriteCallback(args)

        verify(exactly = 1) {
            gattServer.sendResponse(
                args.device,
                args.requestId,
                BluetoothGatt.GATT_SUCCESS,
                args.offset,
                args.value
            )
        }
    }

    @Test
    fun `does not send success response if response is not needed`() {
        val (callbackSlot, gattServer) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        callbackSlot.captured.invokeDescriptorWriteCallback(
            OnDescriptorWriteRequestArgs(responseNeeded = false)
        )

        verify(exactly = 0) {
            gattServer.sendResponse(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `emits UnsupportedEvent for unhandled events`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        manager.events.test {
            callbackSlot.captured.onConnectionStateChange(
                device,
                BluetoothGatt.GATT_FAILURE,
                BluetoothProfile.STATE_CONNECTED
            )

            assertEquals(
                GattServerEvent.UnsupportedEvent(
                    address = DEVICE_ADDRESS,
                    status = BluetoothGatt.GATT_FAILURE,
                    newState = BluetoothProfile.STATE_CONNECTED
                ),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `gatt server returns error if permissions are not granted`() = runTest {
        fakePermissionChecker.hasPeripheralPermissions = false

        manager.events.test {
            manager.open(uuid)

            assertEquals(
                GattServerEvent.Error(
                    GattServerError.BLUETOOTH_PERMISSION_MISSING
                ),
                awaitItem()
            )
        }
    }

    private fun BluetoothGattServerCallback.invokeDescriptorWriteCallback(
        args: OnDescriptorWriteRequestArgs
    ) {
        onDescriptorWriteRequest(
            args.device,
            args.requestId,
            args.descriptor,
            args.preparedWrite,
            args.responseNeeded,
            args.offset,
            args.value
        )
    }
}
