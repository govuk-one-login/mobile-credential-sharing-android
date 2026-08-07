package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.testValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.advertising.AdvertisingError
import uk.gov.onelogin.sharing.bluetooth.api.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerError
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.AndroidPeripheralBluetoothTransportMatchers.hasMonitoringJob
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.hasAddress
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.isConnected
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.isDisconnected
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothStateMatchers.isError
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.FakeGattServerManager
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.coroutines.JobMatchers.isActive

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class AndroidPeripheralBluetoothTransportTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val advertiser = FakeBleAdvertiser()
    private val gattServerManager = FakeGattServerManager()
    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val testScope = CoroutineScope(SupervisorJob() + dispatcherRule.testDispatcher)
    private val logger = SystemLogger()
    private val transport = AndroidPeripheralBluetoothTransport(
        bleAdvertiser = advertiser,
        gattServerManager = gattServerManager,
        bluetoothStateMonitor = bluetoothStateMonitor,
        coroutineScope = testScope,
        logger = logger,
        ioDispatcher = testScope.coroutineContext
    )
    private val uuid = UUID.randomUUID()

    @Test
    fun `advertiser started logs without emitting state`() = runTest(testScope.coroutineContext) {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())
            advertiser.emitState(AdvertiserState.Started)
            advanceUntilIdle()

            expectNoEvents()
        }

        assert(logger.contains("Advertising Started"))
    }

    @Test
    fun `advertiser stopped logs without emitting state`() = runTest(testScope.coroutineContext) {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())
            advertiser.emitState(AdvertiserState.Stopped)
            advanceUntilIdle()

            expectNoEvents()
        }
        assert(logger.contains("Advertising Stopped"))
    }

    @Test
    fun `advertiser idle logs without emitting state`() = runTest(testScope.coroutineContext) {
        transport.monitoringJob.start()

        transport.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())
            advertiser.emitState(AdvertiserState.Idle)
            advanceUntilIdle()

            expectNoEvents()
        }
        assert(logger.contains("Advertising Idle"))
    }

    @Test
    fun `advertiser failure emits error state`() = runTest(testScope.coroutineContext) {
        transport.monitoringJob.start()
        transport.state.test {
            assertEquals(PeripheralBluetoothState.Idle, awaitItem())

            advertiser.emitState(AdvertiserState.Failed("error"))
            advanceUntilIdle()

            assertEquals(
                PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.ADVERTISING_FAILED
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun `Starting the transport defers to opening the GATT Server`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        transport.monitoringJob.start()
        transport.start(uuid)
        advanceUntilIdle()

        assertThat(
            gattServerManager.openCalls,
            equalTo(1)
        )

        assertFalse { transport.isServiceReady }
    }

    @Test
    fun `Begins advertising when receiving 'ServiceAdded' events`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        assertFalse { transport.isServiceReady }
        transport.monitoringJob.start()
        val service: BluetoothGattService = mockk()
        every { service.uuid } returns uuid

        val event = GattServerEvent.ServiceAdded(service)
        gattServerManager.emitEvent(event)
        advanceUntilIdle()

        advertiser.state.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(AdvertiserState.Started)
            )
        }

        assertTrue { transport.isServiceReady }
        assertTrue { "Completed handling gatt server event: $event" in logger }
    }

    @Test
    fun `Advertising state changes are logged`(
        @TestParameter event: AdvertiserState = testValues(
            AdvertiserState.Idle,
            AdvertiserState.Stopped,
            AdvertiserState.Stopping,
            AdvertiserState.Failed("error"),
            AdvertiserState.Starting,
            AdvertiserState.Started
        )
    ) = runTest(dispatcherRule.testDispatcher) {
        transport.monitoringJob.start()
        advertiser.emitState(event)
        advanceUntilIdle()

        assertTrue { "Advertising ${event::class.java.simpleName}" in logger }
    }

    @Test
    fun `GATT Server event changes are logged`(
        @TestParameter event: GattServerEvent = testValues(
            GattServerEvent.Connected(uuid.toString()),
            GattServerEvent.Disconnected(uuid.toString(), false),
            GattServerEvent.ServiceAdded(mockk(relaxed = true)),
            GattServerEvent.MessageReceived(byteArrayOf()),
            GattServerEvent.SessionStarted,
            GattServerEvent.ServiceStopped,
            GattServerEvent.Error(GattServerError.GATT_NOT_AVAILABLE),
            GattServerEvent.SessionEnd(SessionEndStates.SUCCESS),
            GattServerEvent.UnsupportedEvent(
                DEVICE_ADDRESS,
                GATT_SUCCESS,
                BluetoothProfile.STATE_CONNECTED
            )
        )
    ) = runTest(dispatcherRule.testDispatcher) {
        transport.monitoringJob.start()
        gattServerManager.emitEvent(event)
        advanceUntilIdle()

        assertTrue { "Completed handling gatt server event: $event" in logger }
    }

    @Test
    fun `Stopping advertising marks the service as 'not ready'`(
        @TestParameter event: AdvertiserState = testValues(
            AdvertiserState.Stopping,
            AdvertiserState.Stopped
        )
    ) = runTest(dispatcherRule.testDispatcher) {
        transport.isServiceReady = true
        transport.monitoringJob.start()

        advertiser.emitState(event)
        advanceUntilIdle()

        assertFalse { transport.isServiceReady }
    }

    @Test
    fun `Disregards connections when the service isn't ready`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        transport.isServiceReady = false
        transport.monitoringJob.start()
        gattServerManager.emitEvent(GattServerEvent.Connected(uuid.toString()))
        advanceUntilIdle()

        assertTrue {
            "Rejecting connection from $uuid - service not ready" in logger
        }
        assertThat(
            gattServerManager.disconnectCalls,
            equalTo(1)
        )
    }

    @Test
    fun `Advertiser exceptions emit an error event`(
        @TestParameter advertisingError: AdvertisingError
    ) = runTest(dispatcherRule.testDispatcher) {
        advertiser.exceptionToThrow = StartAdvertisingException(advertisingError)
        transport.monitoringJob.start()

        val service: BluetoothGattService = mockk()
        every { service.uuid } returns uuid
        gattServerManager.emitEvent(GattServerEvent.ServiceAdded(service))
        advanceUntilIdle()

        transport.state.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(
                    PeripheralBluetoothState.Error(
                        PeripheralBluetoothTransportError.ADVERTISING_FAILED
                    )
                )
            )
        }

        assertTrue {
            logger.any { it.message.startsWith("Error starting advertising") }
        }
    }

    @Test
    fun `stop calls advertiser stop and gatt server close`() = runTest(testScope.coroutineContext) {
        transport.monitoringJob.start()
        gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
        advanceUntilIdle()

        assertThat(
            transport,
            hasMonitoringJob(isActive())
        )

        transport.stop(
            serviceUuid = uuid,
            sendEndCommand = true
        )
        advanceUntilIdle()

        assertEquals(PeripheralBluetoothState.Idle, transport.state.value)
        assertEquals(1, advertiser.stopCalls)
        assertEquals(1, gattServerManager.closeCalls)
        assertEquals(1, bluetoothStateMonitor.stopCalls)

        assertThat(
            transport,
            hasMonitoringJob(isActive(false))
        )
    }

    @Test
    fun `gatt Connected event triggers mdoc session Connected`() =
        runTest(testScope.coroutineContext) {
            transport.monitoringJob.start()

            transport.state.test {
                assertEquals(PeripheralBluetoothState.Idle, awaitItem())

                gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
                advanceUntilIdle()

                assertEquals(
                    PeripheralBluetoothState.Connected(DEVICE_ADDRESS),
                    awaitItem()
                )
            }
        }

    @Test
    fun `gatt service added event logs without emitting state`() =
        runTest(testScope.coroutineContext) {
            transport.monitoringJob.start()

            val service = mockk<BluetoothGattService>()
            every { service.uuid } returns uuid

            val event = GattServerEvent.ServiceAdded(
                service
            )
            transport.state.test {
                assertEquals(PeripheralBluetoothState.Idle, awaitItem())

                gattServerManager.emitEvent(event)
                advanceUntilIdle()

                expectNoEvents()
            }
        }

    @Test
    fun `gatt Disconnected event triggers mdoc session Disconnected`() =
        runTest(testScope.coroutineContext) {
            transport.monitoringJob.start()
            gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
            advanceUntilIdle()

            transport.state.test {
                assertThat(
                    expectMostRecentItem(),
                    allOf(
                        isConnected(),
                        hasAddress(DEVICE_ADDRESS)
                    )
                )
            }

            gattServerManager.emitEvent(GattServerEvent.Disconnected(DEVICE_ADDRESS, false))
            advanceUntilIdle()

            transport.state.test {
                assertThat(
                    expectMostRecentItem(),
                    allOf(
                        isDisconnected(),
                        hasAddress(DEVICE_ADDRESS)
                    )
                )
            }
        }

    @Test
    fun `duplicate gatt Connected for same device does not emit duplicate Connected state`() =
        runTest(testScope.coroutineContext) {
            transport.monitoringJob.start()

            gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
            advanceUntilIdle()

            transport.state.test {
                assertEquals(
                    PeripheralBluetoothState.Connected(DEVICE_ADDRESS),
                    expectMostRecentItem()
                )
                expectNoEvents()

                gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
                advanceUntilIdle()
                expectNoEvents()
            }
        }

    @Test
    fun `gatt Error event maps to session Error state`() = runTest(testScope.coroutineContext) {
        transport.monitoringJob.start()
        gattServerManager.emitEvent(
            GattServerEvent.Error(
                GattServerError.GATT_NOT_AVAILABLE
            )
        )

        advanceUntilIdle()

        transport.state.test {
            assertEquals(
                PeripheralBluetoothState.Error(
                    PeripheralBluetoothTransportError.GATT_NOT_AVAILABLE
                ),
                expectMostRecentItem()
            )
        }
    }

    @Test
    fun `gatt UnsupportedEvent does not change session state`() =
        runTest(testScope.coroutineContext) {
            gattServerManager.emitEvent(
                GattServerEvent.UnsupportedEvent(
                    address = DEVICE_ADDRESS,
                    status = 999,
                    newState = 42
                )
            )
            advanceUntilIdle()

            transport.state.test {
                assertEquals(PeripheralBluetoothState.Idle, expectMostRecentItem())
            }
        }

    @Test
    fun `gatt SessionStarted does not change session state`() =
        runTest(testScope.coroutineContext) {
            transport.state.test {
                assertEquals(PeripheralBluetoothState.Idle, awaitItem())

                gattServerManager.emitEvent(GattServerEvent.SessionStarted)
                advanceUntilIdle()

                expectNoEvents()
            }
        }

    @Test
    fun `gatt ServiceStopped logs without emitting state`() = runTest(testScope.coroutineContext) {
        transport.monitoringJob.start()
        gattServerManager.emitEvent(GattServerEvent.ServiceStopped)
        advanceUntilIdle()

        transport.state.test {
            assertThat(
                expectMostRecentItem(),
                equalTo(PeripheralBluetoothState.Idle)
            )
        }

        assert(logger.contains("GattService Stopped"))
    }

    @Test
    fun `bluetooth switched off stops BLE session`() = runTest(testScope.coroutineContext) {
        transport.monitoringJob.start()
        bluetoothStateMonitor.start()
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)
        advanceUntilIdle()

        transport.state.test {
            assertThat(
                expectMostRecentItem(),
                isError(PeripheralBluetoothTransportError.BLUETOOTH_TURNED_OFF)
            )
        }
    }

    @Test
    fun `Enabled bluetooth doesn't emit a Peripheral bluetooth event`() =
        runTest(testScope.coroutineContext) {
            transport.monitoringJob.start()
            bluetoothStateMonitor.emit(BluetoothStatus.ON)

            transport.state.test {
                assertThat(
                    expectMostRecentItem(),
                    equalTo(PeripheralBluetoothState.Idle)
                )
            }
        }

    @Test
    fun `Starting the transport after the service is ready marks it as unready`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        transport.isServiceReady = true
        transport.start(uuid)

        assertFalse { transport.isServiceReady }
    }
}
