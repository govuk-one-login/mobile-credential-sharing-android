package uk.gov.onelogin.sharing.bluetooth.api.central.mdoc

import android.bluetooth.BluetoothDevice
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.AndroidCentralBluetoothTransportMatchers.hasMonitoringJob
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.AndroidCentralBluetoothTransportMatchers.hasScanJob
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothStateMatchers.isError
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.ClientError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.central.GattClientEvent
import uk.gov.onelogin.sharing.bluetooth.api.scanner.FakeAndroidBluetoothScanner
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScanEvent
import uk.gov.onelogin.sharing.bluetooth.api.scanner.ScannerFailure
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.central.FakeGattClientManager
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.coroutines.JobMatchers.isActive

class AndroidCentralBluetoothTransportTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val gattClientManager = FakeGattClientManager()
    private val scannerFlow = MutableSharedFlow<ScanEvent>()
    private val scanner = FakeAndroidBluetoothScanner(scannerFlow)
    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val testScope = TestScope(dispatcherRule.testDispatcher)
    private val logger = SystemLogger()

    private val transport by lazy {
        AndroidCentralBluetoothTransport(
            gattClientManager = gattClientManager,
            scanner = scanner,
            bluetoothStateMonitor = bluetoothStateMonitor,
            coroutineScope = testScope.backgroundScope,
            logger = logger,
            ioDispatcher = dispatcherRule.testDispatcher
        )
    }

    private val serviceUuid = java.util.UUID.randomUUID()

    @Test
    fun `initial state is Idle`() = runTest {
        assertEquals(CentralBluetoothState.Idle, transport.state.value)
    }

    @Test
    fun `scanAndConnect sets state to Scanning and starts bluetooth monitor`() = runTest {
        transport.start(serviceUuid)

        assertEquals(CentralBluetoothState.Scanning, transport.state.value)
        assertEquals(1, bluetoothStateMonitor.startCalls)
        assertEquals(1, scanner.scanCalls)
    }

    @Test
    fun `start connects when device found`() = testScope.runTest {
        val device = mockk<BluetoothDevice> {
            every { address } returns DEVICE_ADDRESS
        }

        transport.start(serviceUuid)
        scannerFlow.emit(ScanEvent.DeviceFound(device))

        assertEquals(1, gattClientManager.connectCalls)
    }

    @Test
    fun `scanAndConnect sets error state when scan fails`() = runTest {
        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            transport.start(serviceUuid)
            assertEquals(CentralBluetoothState.Scanning, awaitItem())

            scannerFlow.emit(ScanEvent.ScanFailed(ScannerFailure.INTERNAL_ERROR))
            assertEquals(
                CentralBluetoothState.Error(CentralBluetoothTransportError.SCAN_FAILED),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt Connecting event maps to Connecting state`() = runTest {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            gattClientManager.emitEvent(GattClientEvent.Connecting)
            assertEquals(CentralBluetoothState.Connecting, awaitItem())
        }
    }

    @Test
    fun `gatt Connected event maps to Connected state`() = runTest {
        transport.monitoringJob.start()
        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            gattClientManager.emitEvent(GattClientEvent.Connected(DEVICE_ADDRESS))
            assertEquals(CentralBluetoothState.Connected(DEVICE_ADDRESS), awaitItem())
        }
    }

    @Test
    fun `gatt Disconnected event maps to Disconnected state`() = runTest {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            gattClientManager.emitEvent(GattClientEvent.Disconnected(DEVICE_ADDRESS, false))
            assertEquals(
                CentralBluetoothState.Disconnected(DEVICE_ADDRESS, false),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt Disconnected with session end flag maps correctly`() = runTest {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            gattClientManager.emitEvent(GattClientEvent.Disconnected(DEVICE_ADDRESS, true))
            assertEquals(
                CentralBluetoothState.Disconnected(DEVICE_ADDRESS, true),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt ConnectionStateStarted event maps to ConnectionStateStarted state`() = runTest {
        transport.monitoringJob.start()
        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            gattClientManager.emitEvent(GattClientEvent.ConnectionStateStarted)
            assertEquals(CentralBluetoothState.ConnectionStateStarted, awaitItem())
        }
    }

    @Test
    fun `gatt Error event maps to Error state`() = runTest {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            gattClientManager.emitEvent(GattClientEvent.Error(ClientError.SERVICE_NOT_FOUND))
            assertEquals(
                CentralBluetoothState.Error(CentralBluetoothTransportError.SERVICE_NOT_FOUND),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt SessionEnd event maps to CentralBluetoothEnded state`() = runTest {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            gattClientManager.emitEvent(GattClientEvent.SessionEnd(SessionEndStates.SUCCESS))
            assertEquals(
                CentralBluetoothState.CentralBluetoothEnded(SessionEndStates.SUCCESS),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt UnsupportedEvent does not change state`() = runTest {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(CentralBluetoothState.Idle, awaitItem())

            gattClientManager.emitEvent(
                GattClientEvent.UnsupportedEvent(DEVICE_ADDRESS, 999, 42)
            )

            expectNoEvents()
        }
    }

    @Test
    fun `stop disconnects gatt client and stops bluetooth monitor`() = runTest {
        transport.stop()

        assertEquals(1, gattClientManager.disconnectCalls)
        assertEquals(1, bluetoothStateMonitor.stopCalls)
    }

    @Test
    fun `stop cancels scan and disconnects`() = testScope.runTest {
        transport.start(serviceUuid)

        assertThat(
            transport,
            allOf(
                hasScanJob(),
                hasMonitoringJob(isActive())
            )
        )

        transport.stop()

        assertEquals(1, gattClientManager.disconnectCalls)
        assertEquals(1, bluetoothStateMonitor.stopCalls)
        assertThat(
            transport,
            allOf(
                hasScanJob(nullValue()),
                hasMonitoringJob(isActive(false))
            )
        )
    }

    @Test
    fun `bluetooth OFF emits a CentralBluetoothState error`() = runTest {
        transport.monitoringJob.start()
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)

        transport.state.test {
            assertThat(
                expectMostRecentItem(),
                isError(CentralBluetoothTransportError.BLUETOOTH_TURNED_OFF)
            )
        }
    }

    @Test
    fun `bluetooth ON doesn't update states`() = runTest {
        transport.monitoringJob.start()

        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        transport.state.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(CentralBluetoothState.Idle)
            )

            expectNoEvents()
        }
    }
}
