package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertisingError
import uk.gov.onelogin.sharing.bluetooth.api.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.FakeGattServerManager
import uk.gov.onelogin.sharing.core.MainDispatcherRule

class AndroidPeripheralBluetoothTransportTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val advertiser = FakeBleAdvertiser()
    private val gattServerManager = FakeGattServerManager()
    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val testScope = CoroutineScope(SupervisorJob() + dispatcherRule.testDispatcher)
    private val logger = SystemLogger()
    private val sessionManager = AndroidPeripheralBluetoothTransport(
        bleAdvertiser = advertiser,
        gattServerManager = gattServerManager,
        bluetoothStateMonitor = bluetoothStateMonitor,
        coroutineScope = testScope,
        logger = logger
    )
    private val uuid = UUID.randomUUID()

    @Test
    fun `advertiser started logs without emitting state`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())
            advertiser.emitState(AdvertiserState.Started)
            expectNoEvents()
        }
        assert(logger.contains("Advertising Started"))
    }

    @Test
    fun `advertiser stopped logs without emitting state`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())
            advertiser.emitState(AdvertiserState.Stopped)
            expectNoEvents()
        }
        assert(logger.contains("Advertising Stopped"))
    }

    @Test
    fun `advertiser idle logs without emitting state`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())
            advertiser.emitState(AdvertiserState.Idle)
            expectNoEvents()
        }
        assert(logger.contains("Idle"))
    }

    @Test
    fun `advertiser failure emits error state`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            advertiser.emitState(AdvertiserState.Failed("error"))
            assertEquals(
                PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.ADVERTISING_FAILED
                ),
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

        val sessionManager = AndroidPeripheralBluetoothTransport(
            bleAdvertiser = advertiser,
            gattServerManager = gattServerManager,
            bluetoothStateMonitor = bluetoothStateMonitor,
            coroutineScope = testScope,
            logger = logger
        )

        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            sessionManager.start(uuid)
            assertEquals(
                PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.ADVERTISING_FAILED
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `stop calls advertiser stop and gatt server close`() = runTest {
        sessionManager.stop(
            serviceUuid = uuid,
            sendEndCommand = true
        )

        assertEquals(1, advertiser.stopCalls)
        assertEquals(1, gattServerManager.closeCalls)
        assertEquals(1, bluetoothStateMonitor.stopCalls)
    }

    @Test
    fun `gatt Connected event triggers mdoc session Connected`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
            assertEquals(
                PeripheralBluetoothState.Connected(DEVICE_ADDRESS),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt service added event logs without emitting state`() = runTest {
        val service = mockk<BluetoothGattService>()
        every { service.uuid } returns uuid

        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            gattServerManager.emitEvent(
                GattServerEvent.ServiceAdded(
                    BluetoothGatt.GATT_SUCCESS,
                    service
                )
            )
            expectNoEvents()
        }
        assert(logger.contains("Service Added: $uuid"))
    }

    @Test
    fun `gatt Disconnected event triggers mdoc session Disconnected`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
            assertEquals(
                PeripheralBluetoothState.Connected(DEVICE_ADDRESS),
                awaitItem()
            )

            gattServerManager.emitEvent(GattServerEvent.Disconnected(DEVICE_ADDRESS, false))
            assertEquals(
                PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false),
                awaitItem()
            )
        }
    }

    @Test
    fun `duplicate gatt Connected for same device does not emit duplicate Connected state`() =
        runTest {
            sessionManager.state.test {
                assertEquals(PeripheralBluetoothState.Idle, awaitItem())

                gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
                assertEquals(
                    PeripheralBluetoothState.Connected(DEVICE_ADDRESS),
                    awaitItem()
                )

                gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))

                expectNoEvents()
            }
        }

    @Test
    fun `gatt Error event maps to session Error state`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            gattServerManager.emitEvent(
                GattServerEvent.Error(
                    GattServerError.GATT_NOT_AVAILABLE
                )
            )
            assertEquals(
                PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.GATT_NOT_AVAILABLE
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt UnsupportedEvent does not change session state`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

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
    fun `gatt SessionStarted does not change session state`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.SessionStarted)

            expectNoEvents()
        }
    }

    @Test
    fun `gatt ServiceStopped logs without emitting state`() = runTest {
        sessionManager.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.ServiceStopped)
            expectNoEvents()
        }
        assert(logger.contains("GattService Stopped"))
    }

    @Test
    fun `bluetooth switched off stops BLE session`() = runTest {
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)

        sessionManager.bluetoothStatus.test {
            assertEquals(BluetoothStatus.OFF, awaitItem())
        }

        assertEquals(1, gattServerManager.closeCalls)
        assertEquals(1, advertiser.stopCalls)
    }

    @Test
    fun `bluetooth switched on triggers Bluetooth ON event`() = runTest {
        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        sessionManager.bluetoothStatus.test {
            assertEquals(BluetoothStatus.ON, awaitItem())
        }
    }
}
