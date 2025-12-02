package uk.gov.onelogin.sharing.bluetooth.internal

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingError
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.internal.util.MainDispatcherRule
import uk.gov.onelogin.sharing.bluetooth.peripheral.FakeGattServerManager

class AndroidMdocSessionManagerTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val advertiser = FakeBleAdvertiser()
    private val gattServerManager = FakeGattServerManager()
    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val testScope = CoroutineScope(SupervisorJob() + dispatcherRule.testDispatcher)
    private val sessionManager = AndroidMdocSessionManager(
        bleAdvertiser = advertiser,
        gattServerManager = gattServerManager,
        bluetoothStateMonitor = bluetoothStateMonitor,
        coroutineScope = testScope
    )
    private val uuid = UUID.randomUUID()

    @Test
    fun `initial state is Idle`() = runTest {
        assertEquals(MdocSessionState.Idle, sessionManager.state.value)
    }

    @Test
    fun `advertiser state maps to session state`() = runTest {
        sessionManager.state.test {
            assertEquals(MdocSessionState.Idle, awaitItem())

            advertiser.emitState(AdvertiserState.Started)
            assertEquals(MdocSessionState.AdvertisingStarted, awaitItem())

            advertiser.emitState(AdvertiserState.Stopped)
            assertEquals(MdocSessionState.AdvertisingStopped, awaitItem())

            advertiser.emitState(AdvertiserState.Failed("error"))
            assertEquals(
                MdocSessionState.Error(MdocSessionError.ADVERTISING_FAILED),
                awaitItem()
            )
        }
    }

    @Test
    fun `start triggers advertiser start and gatt server open`() = runTest {
        sessionManager.start(uuid)

        assertEquals(1, advertiser.startCalls)
        assertEquals(uuid, advertiser.lastAdvertiseData?.serviceUuid)
        assertEquals(AdvertiserState.Started, advertiser.state.value)
        assertEquals(1, gattServerManager.openCalls)
        assertEquals(1, bluetoothStateMonitor.startCalls)
    }

    @Test
    fun `start sets Error state when advertiser throws`() = runTest {
        val advertiser = FakeBleAdvertiser().apply {
            exceptionToThrow = StartAdvertisingException(AdvertisingError.INTERNAL_ERROR)
        }

        val sessionManager = AndroidMdocSessionManager(
            bleAdvertiser = advertiser,
            gattServerManager = gattServerManager,
            bluetoothStateMonitor = bluetoothStateMonitor,
            coroutineScope = testScope
        )

        sessionManager.state.test {
            assertEquals(MdocSessionState.Idle, awaitItem())

            sessionManager.start(uuid)
            assertEquals(
                MdocSessionState.Error(MdocSessionError.ADVERTISING_FAILED),
                awaitItem()
            )
        }
    }

    @Test
    fun `stop calls advertiser stop and sets session state to stopped`() = runTest {
        val advertiser = FakeBleAdvertiser(initialState = AdvertiserState.Started)
        val sessionManager = AndroidMdocSessionManager(
            bleAdvertiser = advertiser,
            gattServerManager = gattServerManager,
            bluetoothStateMonitor = bluetoothStateMonitor,
            coroutineScope = testScope
        )

        sessionManager.state.test {
            assertEquals(MdocSessionState.AdvertisingStarted, awaitItem())

            sessionManager.stop()

            assertEquals(1, advertiser.stopCalls)
            assertEquals(MdocSessionState.AdvertisingStopped, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.ServiceStopped)
            assertEquals(1, gattServerManager.closeCalls)
            assertEquals(MdocSessionState.GattServiceStopped, awaitItem())

            assertEquals(1, bluetoothStateMonitor.stopCalls)
        }
    }

    @Test
    fun `gatt Connected event triggers mdoc session Connected`() = runTest {
        sessionManager.state.test {
            assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
            assertEquals(
                MdocSessionState.Connected(DEVICE_ADDRESS),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt service added event triggers mdoc session service added`() = runTest {
        val service = mockk<BluetoothGattService>()
        every { service.uuid } returns uuid

        sessionManager.state.test {
            assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(
                GattServerEvent.ServiceAdded(
                    BluetoothGatt.GATT_SUCCESS,
                    service
                )
            )
            assertEquals(
                MdocSessionState.ServiceAdded(service.uuid),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt Disconnected event riggers mdoc session Disconnected`() = runTest {
        sessionManager.state.test {
            assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
            assertEquals(
                MdocSessionState.Connected(DEVICE_ADDRESS),
                awaitItem()
            )

            gattServerManager.emitEvent(GattServerEvent.Disconnected(DEVICE_ADDRESS))
            assertEquals(
                MdocSessionState.Disconnected(DEVICE_ADDRESS),
                awaitItem()
            )
        }
    }

    @Test
    fun `duplicate gatt Connected for same device does not emit duplicate Connected state`() =
        runTest {
            sessionManager.state.test {
                assertEquals(MdocSessionState.Idle, awaitItem())

                gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
                assertEquals(
                    MdocSessionState.Connected(DEVICE_ADDRESS),
                    awaitItem()
                )

                gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))

                expectNoEvents()
            }
        }

    @Test
    fun `gatt Disconnected for unknown device does not emit Disconnected state`() = runTest {
        sessionManager.state.test {
            assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.Disconnected(DEVICE_ADDRESS))

            expectNoEvents()
        }
    }

    @Test
    fun `gatt Error event maps to session Error state`() = runTest {
        sessionManager.state.test {
            assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(
                GattServerEvent.Error(
                    MdocSessionError.GATT_NOT_AVAILABLE
                )
            )
            assertEquals(
                MdocSessionState.Error(MdocSessionError.GATT_NOT_AVAILABLE),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt UnsupportedEvent does not change session state`() = runTest {
        sessionManager.state.test {
            assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(
                GattServerEvent.UnsupportedEvent(
                    address = DEVICE_ADDRESS,
                    status = 999,
                    newState = 42
                )
            )

            expectNoEvents()
        }
    }

    @Test
    fun `bluetooth switched off triggers event and stops session`() = runTest {
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)

        sessionManager.bluetoothStatus.test {
            assertEquals(BluetoothStatus.OFF, awaitItem())
        }

        sessionManager.state.test {
            assertEquals(MdocSessionState.AdvertisingStopped, awaitItem())
        }

        assertEquals(1, gattServerManager.closeCalls)
        assertEquals(1, advertiser.stopCalls)
    }

    @Test
    fun `should return true if bluetooth enabled`() {
        advertiser.apply {
            mockBluetoothEnabled = true
        }

        assertTrue { sessionManager.isBluetoothEnabled() }
    }

    @Test
    fun `should return false if bluetooth disabled`() {
        advertiser.apply {
            mockBluetoothEnabled = false
        }

        assertFalse { sessionManager.isBluetoothEnabled() }
    }
}
