package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.annotation.RequiresPermission
import app.cash.turbine.test
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.LAST_PART
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.SessionEndStateQueued
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.central.FakeGattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.DescriptorWriteRequestStub.OnDescriptorWriteRequestArgs
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceDefinition
import uk.gov.onelogin.sharing.core.permission.FakePermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toDeniedPermission

class AndroidGattServerManagerTest {
    private val context = mockk<Context>(relaxed = true)
    private val bluetoothManager = mockk<BluetoothManager>(relaxed = true)
    private val device = mockk<BluetoothDevice>().also {
        every { it.address } returns DEVICE_ADDRESS
    }
    private val descriptor = mockk<BluetoothGattDescriptor>()
    private val fakeGattService = AndroidGattServiceBuilder.build(
        GattServiceDefinition(
            UUID.randomUUID(),
            listOf()
        )
    )
    private val permissionResponse = mutableListOf<PermissionCheckerV2.PermissionCheckResult>()
    private val fakePermissionChecker = FakePermissionChecker { permissionResponse }
    private val fakeGattWriter = FakeGattWriter()
    private val logger = SystemLogger()

    private lateinit var manager: AndroidGattServerManager
    private val uuid = UUID.randomUUID()

    @Before
    fun setup() {
        manager = AndroidGattServerManager(
            context = context,
            bluetoothManager = bluetoothManager,
            gattServiceFactory = { fakeGattService },
            permissionsChecker = fakePermissionChecker,
            logger = logger,
            gattWriter = fakeGattWriter
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
                GattServerEvent.Disconnected(DEVICE_ADDRESS, false),
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
            CharacteristicWriteRequestStub.writeRequestStart(
                bluetoothDevice = device,
                characteristic = mockk {
                    every { uuid } returns GattUuids.STATE_UUID
                }
            ).run {
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

        val args = OnDescriptorWriteRequestArgs(
            device = device,
            descriptor = descriptor
        )
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
            OnDescriptorWriteRequestArgs(
                device = device,
                descriptor = descriptor,
                responseNeeded = false
            )
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
        listOf(
            Manifest.permission.BLUETOOTH
        ).toDeniedPermission().let(permissionResponse::addAll)

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
            device,
            args.requestId,
            descriptor,
            args.preparedWrite,
            args.responseNeeded,
            args.offset,
            args.value
        )
    }

    private fun setupOpenGattServer(
        bluetoothManager: BluetoothManager,
        context: Context,
        gattServer: BluetoothGattServer = mockk(relaxed = true)
    ): Pair<CapturingSlot<BluetoothGattServerCallback>, BluetoothGattServer> {
        val callbackSlot = slot<BluetoothGattServerCallback>()
        every {
            bluetoothManager.openGattServer(context, capture(callbackSlot))
        } returns gattServer
        return callbackSlot to gattServer
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun setupNullGattServer(bluetoothManager: BluetoothManager, context: Context) {
        every {
            bluetoothManager.openGattServer(context, any())
        } returns null
    }

    @Test
    fun `emits session end event when end command received`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        manager.events.test {
            CharacteristicWriteRequestStub.writeRequestEnd(
                bluetoothDevice = device,
                characteristic = mockk {
                    every { uuid } returns GattUuids.STATE_UUID
                }
            ).run {
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
                GattServerEvent.SessionEnd(SessionEndStates.SUCCESS),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sends BLE session end command successfully when device connected`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        callbackSlot.captured.onConnectionStateChange(
            device,
            BluetoothGatt.GATT_SUCCESS,
            BluetoothProfile.STATE_CONNECTED
        )

        val result = manager.notifySessionEnd(uuid)

        assertEquals(SessionEndStateQueued.Success, result)
    }

    @Test
    fun `does not send BLE session end command when no device connected`() = runTest {
        manager.open(uuid)

        val result = manager.notifySessionEnd(uuid)

        assertEquals(SessionEndStateQueued.NoDeviceConnected, result)
    }

    @Test
    fun `returns failed state when BLE session end command fails to queue command`() = runTest {
        val fakeGattWriter = FakeGattWriter(success = false)
        val manager = AndroidGattServerManager(
            context = context,
            bluetoothManager = bluetoothManager,
            gattServiceFactory = { fakeGattService },
            permissionsChecker = fakePermissionChecker,
            logger = logger,
            gattWriter = fakeGattWriter
        )
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)

        manager.open(uuid)

        callbackSlot.captured.onConnectionStateChange(
            device,
            BluetoothGatt.GATT_SUCCESS,
            BluetoothProfile.STATE_CONNECTED
        )

        val result = manager.notifySessionEnd(uuid)

        assertEquals(SessionEndStateQueued.Failed, result)
    }

    @Test
    fun `emits message received event`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)

        manager.events.test {
            manager.open(uuid)

            CharacteristicWriteRequestStub.writeRequestMessage(
                bluetoothDevice = device,
                characteristic = mockk {
                    every { uuid } returns GattUuids.CLIENT_2_SERVER_UUID
                },
                message = byteArrayOf(LAST_PART, 0x44, 0x55)
            ).run {
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
                GattServerEvent.MessageReceived(byteArrayOf(0x44, 0x55)),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error event if value is null`() = runTest {
        val (callbackSlot) = setupOpenGattServer(bluetoothManager, context)

        manager.events.test {
            manager.open(uuid)

            OnDescriptorWriteRequestArgs(
                device = device,
                descriptor = descriptor
            ).run {
                callbackSlot.captured.onDescriptorWriteRequest(
                    device,
                    requestId,
                    descriptor,
                    preparedWrite,
                    responseNeeded,
                    offset,
                    null
                )
            }

            assertEquals(
                GattServerEvent.Error(
                    GattServerError.DESCRIPTOR_WRITE_REQUEST_FAILED
                ),
                awaitItem()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }
}
