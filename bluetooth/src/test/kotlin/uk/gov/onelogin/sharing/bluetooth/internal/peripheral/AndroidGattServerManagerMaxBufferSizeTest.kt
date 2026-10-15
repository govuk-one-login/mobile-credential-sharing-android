package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.central.FakeGattWriter
import uk.gov.onelogin.sharing.bluetooth.internal.core.GattUuids
import uk.gov.onelogin.sharing.bluetooth.internal.core.LAST_PART
import uk.gov.onelogin.sharing.bluetooth.internal.core.MessageReassembler
import uk.gov.onelogin.sharing.bluetooth.internal.core.NON_LAST_PART
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.gattcallbacks.CharacteristicWriteRequestStub.writeRequestMessage
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.AndroidGattServiceBuilder
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.service.GattServiceDefinition
import uk.gov.onelogin.sharing.bluetooth.internal.util.MainDispatcherRule
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidGattServerManagerMaxBufferSizeTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mockk<Context>(relaxed = true)
    private val bluetoothManager = mockk<BluetoothManager>(relaxed = true)
    private val testScope = TestScope(mainDispatcherRule.testDispatcher)
    private val device = mockk<BluetoothDevice>().also {
        every { it.address } returns DEVICE_ADDRESS
    }
    private val fakeGattService = AndroidGattServiceBuilder.build(
        GattServiceDefinition(UUID.randomUUID(), listOf())
    )
    private val fakePermissionChecker = FakePermissionChecker { emptyList() }
    private val fakeGattWriter = FakeGattWriter()
    private val logger = SystemLogger()
    private val uuid = UUID.randomUUID()

    private lateinit var manager: AndroidGattServerManager
    private lateinit var callbackSlot: CapturingSlot<BluetoothGattServerCallback>

    @Before
    fun setup() {
        callbackSlot = slot()
        every {
            bluetoothManager.openGattServer(context, capture(callbackSlot))
        } returns mockk<BluetoothGattServer>(relaxed = true)
    }

    @Test
    fun `multi-part message within limit emits MessageReceived`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(30) { 0x11 })
            sendChunk(byteArrayOf(LAST_PART) + ByteArray(30) { 0x22 })

            assertEquals(
                GattServerEvent.MessageReceived(ByteArray(30) { 0x11 } + ByteArray(30) { 0x22 }),
                awaitItem()
            )
        }
    }

    @Test
    fun `single-part message within limit emits MessageReceived`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(LAST_PART) + ByteArray(50) { 0x33 })

            assertEquals(
                GattServerEvent.MessageReceived(ByteArray(50) { 0x33 }),
                awaitItem()
            )
        }
    }

    @Test
    fun `message exactly at limit is accepted`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(50) { 0x11 })
            sendChunk(byteArrayOf(LAST_PART) + ByteArray(50) { 0x22 })

            assertEquals(
                GattServerEvent.MessageReceived(ByteArray(50) { 0x11 } + ByteArray(50) { 0x22 }),
                awaitItem()
            )
        }
    }

    @Test
    fun `NON_LAST_PART chunk exceeding limit emits error`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(101) { 0x11 })
            testScope.advanceUntilIdle()

            assertEquals(
                GattServerEvent.Error(GattServerError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
        }
    }

    @Test
    fun `single LAST_PART chunk exceeding limit emits error`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(LAST_PART) + ByteArray(101) { 0x11 })
            testScope.advanceUntilIdle()

            assertEquals(
                GattServerEvent.Error(GattServerError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
        }
    }

    @Test
    fun `buffer is discarded after exceeding limit`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(101) { 0x11 })
            testScope.advanceUntilIdle()

            assertEquals(
                GattServerEvent.Error(GattServerError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
            // Subsequent chunks are ignored while terminating.
            sendChunk(byteArrayOf(LAST_PART) + ByteArray(10) { 0x22 })
            expectNoEvents()
        }
    }

    private suspend fun testEvents(
        maxReceiveBufferSize: Int = 100,
        validate: suspend TurbineTestContext<GattServerEvent>.() -> Unit
    ) {
        manager = AndroidGattServerManager(
            context = context,
            bluetoothManager = bluetoothManager,
            gattServiceFactory = { fakeGattService },
            permissionsChecker = fakePermissionChecker,
            logger = logger,
            gattWriter = fakeGattWriter,
            coroutineScope = testScope,
            reassemblerFactory = { max -> MessageReassembler(max, logger) },
            maxReceiveBufferSize = maxReceiveBufferSize
        )

        manager.open(uuid)
        callbackSlot.captured.onServiceAdded(BluetoothGatt.GATT_SUCCESS, fakeGattService)
        callbackSlot.captured.onConnectionStateChange(
            device,
            BluetoothGatt.GATT_SUCCESS,
            BluetoothProfile.STATE_CONNECTED
        )

        manager.events.test {
            validate()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun sendChunk(message: ByteArray) {
        writeRequestMessage(
            bluetoothDevice = device,
            characteristic = mockk {
                every { uuid } returns GattUuids.CLIENT_2_SERVER_UUID
            },
            message = message
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
    }
}
