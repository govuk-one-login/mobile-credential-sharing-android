package uk.gov.onelogin.sharing.bluetooth.internal.central

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Build
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyCount
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasProperty
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.LAST_PART
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerCallback.Companion.NON_LAST_PART
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids.CLIENT_2_SERVER_UUID
import uk.gov.onelogin.sharing.bluetooth.internal.core.MtuValues
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState
import uk.gov.onelogin.sharing.bluetooth.internal.validator.FakeServiceValidator
import uk.gov.onelogin.sharing.core.permission.FakePermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2
import uk.gov.onelogin.sharing.core.permission.PermissionsToResultExt.toDeniedPermission
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDto

@RunWith(RobolectricTestRunner::class)
@Suppress("LargeClass")
internal class AndroidGattClientManagerTest {
    private val context = mockk<Context>(relaxed = true)
    private val bluetoothDevice = mockk<BluetoothDevice>(relaxed = true)
    private val bluetoothGatt = mockk<BluetoothGatt>(relaxed = true)
    private val permissionResponse = mutableListOf<PermissionCheckerV2.PermissionCheckResult>()
    private val fakePermissionChecker = FakePermissionChecker { permissionResponse }
    private val fakeGattWriter = FakeGattWriter()

    private val fakeServiceValidator = FakeServiceValidator()
    private val logger = SystemLogger()
    private val uuid = UUID.randomUUID()

    private lateinit var manager: AndroidGattClientManager

    private fun createManager(gattWriter: GattWriter) = AndroidGattClientManager(
        context,
        fakePermissionChecker,
        fakeServiceValidator,
        gattWriter,
        logger
    )

    @Before
    fun setup() {
        manager = createManager(fakeGattWriter)
    }

    @Test
    fun `returns error if permission is not granted`() = runTest {
        listOf(
            Manifest.permission.BLUETOOTH
        ).toDeniedPermission().let(permissionResponse::addAll)

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
        testEvents { callbackSlot ->
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
        every { bluetoothGatt.getService(any()) } returns null

        testEvents { callbackSlot ->
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

        every {
            bluetoothGatt.setCharacteristicNotification(any(), true)
        } returns true

        manager.events.test {
            manager.connect(
                bluetoothDevice,
                uuid
            )

            assertEquals(
                GattClientEvent.Connecting,
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
    fun `requests max possible transmission unit when service discovery is successful`() = runTest {
        every {
            bluetoothGatt.setCharacteristicNotification(any(), true)
        } returns true

        testEvents { callbackSlot ->
            callbackSlot.captured.onServicesDiscovered(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS
            )

            verify { bluetoothGatt.requestMtu(MtuValues.MAX_MTU) }
        }
    }

    @Test
    fun `subscribes to state changes when service discovery is successful`() = runTest {
        val service = setupBluetoothGattService()

        every {
            bluetoothGatt.setCharacteristicNotification(any(), true)
        } returns true

        val stateCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateCharacteristic

        testEvents { callbackSlot ->
            callbackSlot.captured.onServicesDiscovered(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS
            )

            verify {
                bluetoothGatt.setCharacteristicNotification(
                    stateCharacteristic,
                    true
                )
            }
        }
    }

    @Test
    fun `emits error when state characteristic does not exist during subscription`() = runTest {
        val service = setupBluetoothGattService()
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns null

        testEvents { callbackSlot ->
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

            assert(
                logger.contains("Gatt Service does not have a state characteristic")
            )
        }
    }

    @Test
    fun `emits error when server to client characteristic does not exist during subscription`() =
        runTest {
            val service = setupBluetoothGattService()
            every { service.getCharacteristic(GattUuids.SERVER_2_CLIENT_UUID) } returns null

            testEvents { callbackSlot ->
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

                assert(
                    logger.contains(INVALID_SERVICE)
                )
            }
        }

    @Test
    fun `subscribes to serverToClient messages when service discovery is successful`() = runTest {
        val service = setupBluetoothGattService()

        every {
            bluetoothGatt.setCharacteristicNotification(any(), true)
        } returns true

        val serverToClientCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every {
            service.getCharacteristic(GattUuids.STATE_UUID)
        } returns serverToClientCharacteristic

        testEvents { callbackSlot ->
            callbackSlot.captured.onServicesDiscovered(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS
            )

            verify {
                bluetoothGatt.setCharacteristicNotification(
                    serverToClientCharacteristic,
                    true
                )
            }
        }
    }

    @Test
    fun `sets state to start when Mtu is agreed`() = runTest {
        setupBluetoothGattService()
        setupCharacteristic(GattUuids.STATE_UUID)

        testEvents { callbackSlot ->
            callbackSlot.captured.onMtuChanged(
                bluetoothGatt,
                MtuValues.MAX_MTU,
                BluetoothGatt.GATT_SUCCESS
            )

            assertEquals(1, fakeGattWriter.writes)

            assertEquals(GattClientEvent.ConnectionStateStarted, awaitItem())
        }
    }

    @Test
    fun `does not set state to start when write characteristic fails`() = runTest {
        val failingWriter = FakeGattWriter(false)
        manager = createManager(failingWriter)

        setupBluetoothGattService()
        setupCharacteristic(GattUuids.STATE_UUID)

        testEvents { callbackSlot ->
            callbackSlot.captured.onMtuChanged(
                bluetoothGatt,
                MtuValues.MAX_MTU,
                BluetoothGatt.GATT_SUCCESS
            )

            assertEquals(1, failingWriter.writes)

            assertNotEquals(GattClientEvent.ConnectionStateStarted, awaitItem())
        }
    }

    @Test
    fun `emits error when state characteristic does not exist when Mtu is agreed`() = runTest {
        val service = setupBluetoothGattService()
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns null

        testEvents { callbackSlot ->
            callbackSlot.captured.onMtuChanged(
                bluetoothGatt,
                MtuValues.MAX_MTU,
                BluetoothGatt.GATT_SUCCESS
            )

            assertEquals(
                GattClientEvent.Error(
                    ClientError.INVALID_SERVICE
                ),
                awaitItem()
            )

            assert(
                logger.contains("Gatt Service does not have a state characteristic")
            )
        }
    }

    @Test
    fun `emits error when subscribing to characteristic fails`() = runTest {
        every {
            bluetoothGatt.setCharacteristicNotification(any(), true)
        } returns false

        testEvents { callbackSlot ->
            callbackSlot.captured.onServicesDiscovered(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS
            )

            assertEquals(
                GattClientEvent.Error(
                    ClientError.FAILED_TO_SUBSCRIBE
                ),
                awaitItem()
            )

            verifyCount { bluetoothGatt.disconnect() }
        }
    }

    @Test
    fun `emits error when start value cannot be written to state characteristic`() = runTest {
        testEvents { callbackSlot ->
            callbackSlot.captured.onCharacteristicWrite(
                bluetoothGatt,
                mockk(relaxed = true),
                BluetoothGatt.GATT_FAILURE
            )

            assertEquals(
                GattClientEvent.Error(
                    ClientError.FAILED_TO_START
                ),
                awaitItem()
            )

            verifyCount { bluetoothGatt.disconnect() }
        }
    }

    @Test
    fun `emits service disconnected`() = runTest {
        testEvents { callbackSlot ->
            callbackSlot.captured.onConnectionStateChange(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS,
                BluetoothGatt.STATE_DISCONNECTED
            )

            assertEquals(
                GattClientEvent.Disconnected(bluetoothGatt.device.address, false),
                awaitItem()
            )
        }
    }

    @Test
    fun `emits unsupported event`() = runTest {
        testEvents { callbackSlot ->
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

    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    @Test
    fun `handles incoming end command and emits SessionEnd event`() = runTest {
        setupBluetoothGattService()

        val endByte = MdocState.END.code

        val stateCharacteristic = setupCharacteristic(GattUuids.STATE_UUID)

        testEvents { callbackSlot ->
            callbackSlot.captured.onCharacteristicChanged(
                bluetoothGatt,
                stateCharacteristic,
                byteArrayOf(endByte)
            )

            assertEquals(GattClientEvent.SessionEnd(SessionEndStates.SUCCESS), awaitItem())
        }
    }

    /**
     * DCMAW-16908: AC1: Process an intermediate message packet (0x01)
     */
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    @Test
    fun `Strips chunked bytes into a completed 'Server2Client' message`() = runTest {
        setupBluetoothGattService()
        val stateCharacteristic = setupCharacteristic(GattUuids.SERVER_2_CLIENT_UUID)

        val expectedBytes = byteArrayOf(
            0,
            64,
            92,
            117,
            10,
            20,
            50
        )
        val inputBytes = expectedBytes.toList().chunked(5).map {
            it.toByteArray()
        }

        testEvents { callbackSlot ->
            inputBytes.forEach { input ->
                val prefixBytes = if (inputBytes.last().contentEquals(input)) {
                    LAST_PART
                } else {
                    NON_LAST_PART
                }

                callbackSlot.captured.onCharacteristicChanged(
                    bluetoothGatt,
                    stateCharacteristic,
                    byteArrayOf(prefixBytes) + input
                )
            }

            assertEquals(
                GattClientEvent.Message(
                    uuid = GattUuids.SERVER_2_CLIENT_UUID,
                    value = expectedBytes
                ),
                awaitItem()
            )

            inputBytes.dropLast(1).forEach { chunk ->
                assertTrue(
                    "Chunked 'Server2Client' characteristic update: ${chunk.toHexString()}"
                        in logger
                )
            }

            assertTrue(
                "Completed 'Server2Client' message transfer:" in logger
            )
        }
    }

    /**
     * DCMAW-16908: AC2: Process a complete or final message packet (0x00)
     */
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    @Test
    fun `Strips 'END' bytes from a completed 'Server2Client' message`() = runTest {
        setupBluetoothGattService()
        val stateCharacteristic = setupCharacteristic(GattUuids.SERVER_2_CLIENT_UUID)
        val expectedBytes = SessionDataDto().toCbor(CborMapper.default)

        testEvents { callbackSlot ->
            callbackSlot.captured.onCharacteristicChanged(
                bluetoothGatt,
                stateCharacteristic,
                byteArrayOf(LAST_PART) + expectedBytes
            )

            assertEquals(
                GattClientEvent.Message(
                    uuid = GattUuids.SERVER_2_CLIENT_UUID,
                    value = expectedBytes
                ),
                awaitItem()
            )
        }

        assertTrue(
            "Completed 'Server2Client' message transfer:" in logger
        )
    }

    /**
     * DCMAW-16908: AC3: Reject a message packet with an invalid header byte
     */
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    @Test
    fun `Message errors occur with invalid status prefixes`() = runTest {
        setupBluetoothGattService()
        val stateCharacteristic = setupCharacteristic(GattUuids.SERVER_2_CLIENT_UUID)
        val expectedBytes = SessionDataDto().toCbor(CborMapper.default)

        testEvents { callbackSlot ->
            callbackSlot.captured.onCharacteristicChanged(
                bluetoothGatt,
                stateCharacteristic,
                byteArrayOf(0x02) + expectedBytes
            )

            assertThat(
                awaitItem(),
                allOf(
                    instanceOf(GattClientEvent.Error::class.java),
                    hasProperty(
                        "error",
                        equalTo(ClientError.INVALID_MESSAGE_PREFIX)
                    )
                )
            )
        }

        assertTrue(
            logger.any {
                it.message.startsWith("Received invalid status byte: ")
            }
        )
    }

    @Test
    fun `writes session end to server and emits SessionEnd`() = runTest {
        val service = setupBluetoothGattService()

        val stateCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateCharacteristic

        testEvents {
            val result = manager.notifySessionEnd()
            assertEquals(SessionEndStates.SUCCESS, result)
        }
    }

    @Test
    fun `writes session handles error`() = runTest {
        val failingWriter = FakeGattWriter(false)
        manager = createManager(failingWriter)

        val service = setupBluetoothGattService()

        val stateCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateCharacteristic

        testEvents {
            val result = manager.notifySessionEnd()
            assertEquals(SessionEndStates.WRITE_TO_SERVER_FAILED, result)
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `writes CCCD descriptor after MTU negotiated`() = runTest {
        val mocks = setupCccdService()

        testEvents { callbackSlot ->
            callbackSlot.discoverServicesAndNegotiateMtu()

            verify { bluetoothGatt.writeDescriptor(mocks.stateDescriptor) }
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `writes next CCCD descriptor after first descriptor write succeeds`() = runTest {
        val mocks = setupCccdService()

        testEvents { callbackSlot ->
            callbackSlot.discoverServicesAndNegotiateMtu()

            callbackSlot.captured.onDescriptorWrite(
                bluetoothGatt,
                mocks.stateDescriptor,
                BluetoothGatt.GATT_SUCCESS
            )

            verify { bluetoothGatt.writeDescriptor(mocks.s2cDescriptor) }
        }
    }

    @Test
    fun `emits ConnectionStateStarted after all descriptors written and mtu negotiated`() =
        runTest {
            val mocks = setupCccdService()

            testEvents { callbackSlot ->
                callbackSlot.discoverServicesAndNegotiateMtu()

                callbackSlot.captured.onDescriptorWrite(
                    bluetoothGatt,
                    mocks.stateDescriptor,
                    BluetoothGatt.GATT_SUCCESS
                )

                callbackSlot.captured.onDescriptorWrite(
                    bluetoothGatt,
                    mocks.s2cDescriptor,
                    BluetoothGatt.GATT_SUCCESS
                )

                assertEquals(GattClientEvent.ConnectionStateStarted, awaitItem())
            }
        }

    @Test
    fun `does not emit ConnectionStateStarted when only mtu negotiated`() = runTest {
        setupCccdService()

        testEvents { callbackSlot ->
            callbackSlot.discoverServicesAndNegotiateMtu()

            expectNoEvents()
        }
    }

    @Test
    fun `does not emit ConnectionStateStarted when only descriptors complete`() = runTest {
        setupCccdService(hasDescriptors = false)

        testEvents { callbackSlot ->
            callbackSlot.captured.onServicesDiscovered(
                bluetoothGatt,
                BluetoothGatt.GATT_SUCCESS
            )

            expectNoEvents()
        }
    }

    @Suppress("DEPRECATION")
    @Test
    fun `changedMtu writes pending descriptors instead of starting state`() = runTest {
        val mocks = setupCccdService()

        testEvents { callbackSlot ->
            callbackSlot.discoverServicesAndNegotiateMtu()

            verify { bluetoothGatt.writeDescriptor(mocks.stateDescriptor) }
            assertEquals(0, fakeGattWriter.writes)
        }
    }

    @Test
    fun `emits error when CCCD descriptor write fails`() = runTest {
        val mocks = setupCccdService()

        testEvents { callbackSlot ->
            callbackSlot.discoverServicesAndNegotiateMtu()

            callbackSlot.captured.onDescriptorWrite(
                bluetoothGatt,
                mocks.stateDescriptor,
                BluetoothGatt.GATT_FAILURE
            )

            assertEquals(
                GattClientEvent.Error(ClientError.FAILED_TO_SUBSCRIBE),
                awaitItem()
            )
        }
    }

    private data class CccdMocks(
        val stateDescriptor: BluetoothGattDescriptor,
        val s2cDescriptor: BluetoothGattDescriptor
    )

    private fun setupCccdService(hasDescriptors: Boolean = true): CccdMocks {
        val service = setupBluetoothGattService()

        every {
            bluetoothGatt.setCharacteristicNotification(any(), true)
        } returns true

        val stateDescriptor = mockk<BluetoothGattDescriptor>(relaxed = true)
        val stateCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every {
            stateCharacteristic.getDescriptor(GattUuids.CLIENT_CHARACTERISTIC_CONFIG_UUID)
        } returns if (hasDescriptors) stateDescriptor else null
        every { service.getCharacteristic(GattUuids.STATE_UUID) } returns stateCharacteristic

        val s2cDescriptor = mockk<BluetoothGattDescriptor>(relaxed = true)
        val s2cCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every {
            s2cCharacteristic.getDescriptor(GattUuids.CLIENT_CHARACTERISTIC_CONFIG_UUID)
        } returns if (hasDescriptors) s2cDescriptor else null
        every { service.getCharacteristic(GattUuids.SERVER_2_CLIENT_UUID) } returns
            s2cCharacteristic

        return CccdMocks(stateDescriptor, s2cDescriptor)
    }

    private fun CapturingSlot<BluetoothGattCallback>.discoverServicesAndNegotiateMtu() {
        captured.onServicesDiscovered(bluetoothGatt, BluetoothGatt.GATT_SUCCESS)
        captured.onMtuChanged(bluetoothGatt, MtuValues.MAX_MTU, BluetoothGatt.GATT_SUCCESS)
    }

    private suspend fun testEvents(
        validate: suspend TurbineTestContext<GattClientEvent>.(
            CapturingSlot<BluetoothGattCallback>
        ) -> Unit
    ) {
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

            validate(callbackSlot)
        }
    }

    private fun setupConnectedGatt(
        gattWriter: GattWriter = fakeGattWriter
    ): Pair<AndroidGattClientManager, CapturingSlot<BluetoothGattCallback>> {
        val callbackSlot = slot<BluetoothGattCallback>()
        every {
            bluetoothDevice.connectGatt(context, any(), capture(callbackSlot), any())
        } returns bluetoothGatt
        val mgr = createManager(gattWriter)
        mgr.connect(bluetoothDevice, uuid)
        return mgr to callbackSlot
    }

    @Test
    fun `sendMessage with data fitting single chunk prepends 0x00 and writes once`() = runTest {
        val (mgr, callbackSlot) = setupConnectedGatt()
        val data = ByteArray(10) { it.toByte() }
        val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        val service = mockk<BluetoothGattService>(relaxed = true)
        every { bluetoothGatt.getService(uuid) } returns service
        every { service.getCharacteristic(CLIENT_2_SERVER_UUID) } returns characteristic
        every { characteristic.uuid } returns CLIENT_2_SERVER_UUID

        launch {
            repeat(1) {
                callbackSlot.captured.onCharacteristicWrite(
                    bluetoothGatt,
                    characteristic,
                    BluetoothGatt.GATT_SUCCESS
                )
            }
        }
        val result = mgr.sendMessage(uuid, data)

        assertEquals(true, result)
        assertEquals(1, fakeGattWriter.writes)
        assertEquals(0x00.toByte(), fakeGattWriter.sentChunks.first().first())
    }

    @Test
    fun `sendMessage with data exceeding chunk size splits and prepends 0x01 then 0x00`() =
        runTest {
            val (mgr, callbackSlot) = setupConnectedGatt()
            callbackSlot.captured.onMtuChanged(
                bluetoothGatt,
                MtuValues.MIN_MTU,
                BluetoothGatt.GATT_SUCCESS
            )
            fakeGattWriter.reset()
            val data = ByteArray(30) { it.toByte() }
            val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
            val service = mockk<BluetoothGattService>(relaxed = true)
            every { bluetoothGatt.getService(uuid) } returns service
            every { service.getCharacteristic(CLIENT_2_SERVER_UUID) } returns characteristic
            every { characteristic.uuid } returns CLIENT_2_SERVER_UUID

            launch {
                repeat(2) {
                    callbackSlot.captured.onCharacteristicWrite(
                        bluetoothGatt,
                        characteristic,
                        BluetoothGatt.GATT_SUCCESS
                    )
                }
            }
            val result = mgr.sendMessage(uuid, data)

            assertEquals(true, result)
            assertEquals(2, fakeGattWriter.writes)
            assertEquals(0x01.toByte(), fakeGattWriter.sentChunks[0].first())
            assertEquals(0x00.toByte(), fakeGattWriter.sentChunks[1].first())
        }

    @Test
    fun `sendMessage returns false and stops when write fails`() = runTest {
        val failingWriter = FakeGattWriter(success = false)
        val (mgr, _) = setupConnectedGatt(failingWriter)
        val data = ByteArray(10) { it.toByte() }
        val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        val service = mockk<BluetoothGattService>(relaxed = true)
        every { bluetoothGatt.getService(uuid) } returns service
        every { service.getCharacteristic(CLIENT_2_SERVER_UUID) } returns characteristic

        val result = mgr.sendMessage(uuid, data)

        assertEquals(false, result)
        assertEquals(1, failingWriter.writes)
        assert(logger.contains("Failed to write SessionEstablishment packet at offset 0"))
    }

    @Test
    fun `sendMessage logs success after final chunk written`() = runTest {
        val (mgr, callbackSlot) = setupConnectedGatt()
        val data = ByteArray(10) { it.toByte() }
        val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        val service = mockk<BluetoothGattService>(relaxed = true)
        every { bluetoothGatt.getService(uuid) } returns service
        every { service.getCharacteristic(CLIENT_2_SERVER_UUID) } returns characteristic
        every { characteristic.uuid } returns CLIENT_2_SERVER_UUID

        launch {
            repeat(1) {
                callbackSlot.captured.onCharacteristicWrite(
                    bluetoothGatt,
                    characteristic,
                    BluetoothGatt.GATT_SUCCESS
                )
            }
        }
        mgr.sendMessage(uuid, data)

        assert(logger.contains("Final SessionEstablishment chunk generated and sent"))
        assert(logger.contains("SessionEstablishment transmission complete"))
    }

    @Test
    fun `sendMessage logs intermediate chunk before final chunk`() = runTest {
        val (mgr, callbackSlot) = setupConnectedGatt()
        callbackSlot.captured.onMtuChanged(
            bluetoothGatt,
            MtuValues.MIN_MTU,
            BluetoothGatt.GATT_SUCCESS
        )
        fakeGattWriter.reset()
        val data = ByteArray(30) { it.toByte() }
        val characteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        val service = mockk<BluetoothGattService>(relaxed = true)
        every { bluetoothGatt.getService(uuid) } returns service
        every { service.getCharacteristic(CLIENT_2_SERVER_UUID) } returns characteristic
        every { characteristic.uuid } returns CLIENT_2_SERVER_UUID

        launch {
            repeat(2) {
                callbackSlot.captured.onCharacteristicWrite(
                    bluetoothGatt,
                    characteristic,
                    BluetoothGatt.GATT_SUCCESS
                )
            }
        }
        mgr.sendMessage(uuid, data)

        assert(
            logger.contains(
                "Intermediate SessionEstablishment chunk generated, more data will follow"
            )
        )
        assert(logger.contains("Final SessionEstablishment chunk generated and sent"))
        assert(logger.contains("SessionEstablishment transmission complete"))
    }

    @Test
    fun `chunk size is MTU minus 3 BLE overhead minus 1 ISO header`() {
        assertEquals(MtuValues.MIN_MTU - 3 - 1, MtuValues.dataChunkSize(MtuValues.MIN_MTU))
    }

    private fun setupBluetoothGattService(
        mock: BluetoothGattService = mockk(relaxed = true)
    ): BluetoothGattService {
        every { bluetoothGatt.getService(any()) } returns mock
        return mock
    }

    private fun setupCharacteristic(uuid: UUID): BluetoothGattCharacteristic {
        val stateCharacteristic = mockk<BluetoothGattCharacteristic>(relaxed = true)
        every { stateCharacteristic.uuid } returns uuid
        return stateCharacteristic
    }
}
