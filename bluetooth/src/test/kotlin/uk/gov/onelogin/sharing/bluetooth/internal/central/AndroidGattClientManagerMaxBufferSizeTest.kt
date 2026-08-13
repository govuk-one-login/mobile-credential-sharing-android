package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.LAST_PART
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.NON_LAST_PART
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState
import uk.gov.onelogin.sharing.bluetooth.internal.util.MainDispatcherRule
import uk.gov.onelogin.sharing.bluetooth.internal.validator.FakeServiceValidator
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
@OptIn(ExperimentalCoroutinesApi::class)
internal class AndroidGattClientManagerMaxBufferSizeTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = mockk<Context>(relaxed = true)
    private val bluetoothDevice = mockk<BluetoothDevice>(relaxed = true)
    private val bluetoothGatt = mockk<BluetoothGatt>(relaxed = true)
    private val fakePermissionChecker = FakePermissionChecker { emptyList() }
    private val fakeGattWriter = FakeGattWriter()
    private val fakeWriteQueue = FakeGattWriteQueue()
    private val fakeServiceValidator = FakeServiceValidator()
    private val logger = SystemLogger()
    private val uuid = UUID.randomUUID()
    private val testScope = TestScope(mainDispatcherRule.testDispatcher)

    private lateinit var manager: AndroidGattClientManager
    private lateinit var server2ClientCharacteristic: BluetoothGattCharacteristic
    private lateinit var callbackSlot: io.mockk.CapturingSlot<BluetoothGattCallback>

    @Before
    fun setup() {
        val service = mockk<BluetoothGattService>(relaxed = true)
        every { bluetoothGatt.getService(any()) } returns service

        server2ClientCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every { server2ClientCharacteristic.uuid } returns GattUuids.SERVER_2_CLIENT_UUID

        val stateCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateCharacteristic

        callbackSlot = slot<BluetoothGattCallback>()
        every {
            bluetoothDevice.connectGatt(context, any(), capture(callbackSlot), any())
        } returns bluetoothGatt
    }

    @Test
    fun `message within limit emits MessageReceived`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(30) { 0x11 })
            sendChunk(byteArrayOf(LAST_PART) + ByteArray(30) { 0x22 })

            assertEquals(
                GattClientEvent.Message(
                    uuid = GattUuids.SERVER_2_CLIENT_UUID,
                    value = ByteArray(30) { 0x11 } + ByteArray(30) { 0x22 }
                ),
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
                GattClientEvent.Message(
                    uuid = GattUuids.SERVER_2_CLIENT_UUID,
                    value = ByteArray(50) { 0x11 } + ByteArray(50) { 0x22 }
                ),
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
                GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
        }
    }

    @Test
    fun `LAST_PART chunk exceeding limit emits error`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(LAST_PART) + ByteArray(101) { 0x11 })
            testScope.advanceUntilIdle()

            assertEquals(
                GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
        }
    }

    @Test
    fun `sends END and disconnects when buffer limit exceeded`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(101) { 0x11 })
            testScope.advanceUntilIdle()

            assertEquals(
                GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )

            assertTrue(fakeGattWriter.sentChunks.isNotEmpty())
            assertEquals(
                MdocState.END.code,
                fakeGattWriter.sentChunks.last()[0]
            )

            verify { bluetoothGatt.disconnect() }
            verify { bluetoothGatt.close() }
        }
    }

    @Test
    fun `disconnects and emits error when buffer exceeded even if END notification fails`() =
        runTest {
            val failingGattWriter = FakeGattWriter(success = false)
            manager = AndroidGattClientManager(
                context,
                fakePermissionChecker,
                fakeServiceValidator,
                failingGattWriter,
                logger,
                fakeWriteQueue,
                testScope,
                maxReceiveBufferSize = 100
            )

            manager.events.test {
                manager.connect(bluetoothDevice, uuid)
                skipItems(1) // Connecting event

                sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(101) { 0x11 })
                testScope.advanceUntilIdle()

                assertEquals(
                    GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE),
                    awaitItem()
                )

                // Teardown happens even though the END notification failed to write
                verify { bluetoothGatt.disconnect() }
                verify { bluetoothGatt.close() }

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `logs buffer size details when limit exceeded`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(101) { 0x11 })
            testScope.advanceUntilIdle()
            awaitItem()
        }

        assertTrue(
            "ExceededMaxBufferSize: 101 bytes exceeds limit of 100 bytes" in logger
        )
    }

    @Test
    fun `custom buffer size is enforced`() = runTest {
        testEvents(maxReceiveBufferSize = 50) {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(51) { 0x11 })
            testScope.advanceUntilIdle()

            assertEquals(
                GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
        }
    }

    @Test
    fun `chunks received after limit exceeded are ignored`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(101) { 0x11 })
            sendChunk(byteArrayOf(LAST_PART) + ByteArray(10) { 0x22 })
            testScope.advanceUntilIdle()

            assertEquals(
                GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
            expectNoEvents()
        }
    }

    @Test
    fun `service changed after limit exceeded is ignored`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(101) { 0x11 })
            callbackSlot.captured.onServiceChanged(bluetoothGatt)
            testScope.advanceUntilIdle()

            assertEquals(
                GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
            expectNoEvents()
        }
    }

    @Test
    fun `new connection resets terminating state`() = runTest {
        testEvents {
            sendChunk(byteArrayOf(NON_LAST_PART) + ByteArray(101) { 0x11 })
            testScope.advanceUntilIdle()

            assertEquals(
                GattClientEvent.Error(ClientError.EXCEEDED_MAX_BUFFER_SIZE),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }

        manager.events.test {
            manager.connect(bluetoothDevice, uuid)
            skipItems(1) // Connecting event

            sendChunk(byteArrayOf(LAST_PART) + ByteArray(10) { 0x33 })

            assertEquals(
                GattClientEvent.Message(
                    uuid = GattUuids.SERVER_2_CLIENT_UUID,
                    value = ByteArray(10) { 0x33 }
                ),
                awaitItem()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createManager(maxReceiveBufferSize: Int = 100) {
        manager = AndroidGattClientManager(
            context,
            fakePermissionChecker,
            fakeServiceValidator,
            fakeGattWriter,
            logger,
            fakeWriteQueue,
            testScope,
            maxReceiveBufferSize = maxReceiveBufferSize
        )
    }

    private suspend fun testEvents(
        maxReceiveBufferSize: Int = 100,
        validate: suspend TurbineTestContext<GattClientEvent>.() -> Unit
    ) {
        createManager(maxReceiveBufferSize)
        manager.events.test {
            manager.connect(bluetoothDevice, uuid)
            skipItems(1) // Connecting event
            validate()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun sendChunk(message: ByteArray) {
        callbackSlot.captured.onCharacteristicChanged(
            bluetoothGatt,
            server2ClientCharacteristic,
            message
        )
    }
}
