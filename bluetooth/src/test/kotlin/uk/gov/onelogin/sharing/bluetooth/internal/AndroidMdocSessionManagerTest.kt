package uk.gov.onelogin.sharing.bluetooth.internal

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattService
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.api.GattServerEvent
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingError
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.internal.util.MainDispatcherRule
import uk.gov.onelogin.sharing.bluetooth.peripheral.FakeGattServerManager
import uk.gov.onelogin.sharing.bluetooth.permissions.StubDeviceAddress.DEVICE_ADDRESS

class AndroidMdocSessionManagerTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val advertiser = FakeBleAdvertiser()
    private val gattServerManager = FakeGattServerManager()
    private val testScope = CoroutineScope(SupervisorJob() + dispatcherRule.testDispatcher)
    private val sessionManager = AndroidMdocSessionManager(
        bleAdvertiser = advertiser,
        gattServerManager = gattServerManager,
        coroutineScope = testScope
    )
    private val uuid = UUID.randomUUID()

    @Test
    fun `initial state is Idle`() = runTest {
        Assert.assertEquals(MdocSessionState.Idle, sessionManager.state.value)
    }

    @Test
    fun `advertiser state maps to session state`() = runTest {
        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            advertiser.emitState(AdvertiserState.Started)
            Assert.assertEquals(MdocSessionState.AdvertisingStarted, awaitItem())

            advertiser.emitState(AdvertiserState.Stopped)
            Assert.assertEquals(MdocSessionState.AdvertisingStopped, awaitItem())

            advertiser.emitState(AdvertiserState.Failed("error"))
            Assert.assertEquals(
                MdocSessionState.Error(MdocSessionError.ADVERTISING_FAILED),
                awaitItem()
            )
        }
    }

    @Test
    fun `start triggers advertiser start and gatt server open`() = runTest {
        sessionManager.start(uuid)

        Assert.assertEquals(1, advertiser.startCalls)
        Assert.assertEquals(uuid, advertiser.lastAdvertiseData?.serviceUuid)
        Assert.assertEquals(AdvertiserState.Started, advertiser.state.value)
        Assert.assertEquals(1, gattServerManager.openCalls)
    }

    @Test
    fun `start sets Error state when advertiser throws`() = runTest {
        val advertiser = FakeBleAdvertiser().apply {
            exceptionToThrow = StartAdvertisingException(AdvertisingError.INTERNAL_ERROR)
        }

        val sessionManager = AndroidMdocSessionManager(
            bleAdvertiser = advertiser,
            gattServerManager = gattServerManager,
            coroutineScope = testScope
        )

        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            sessionManager.start(uuid)
            Assert.assertEquals(
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
            coroutineScope = testScope
        )

        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.AdvertisingStarted, awaitItem())

            sessionManager.stop()

            Assert.assertEquals(1, advertiser.stopCalls)
            Assert.assertEquals(MdocSessionState.AdvertisingStopped, awaitItem())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `gatt Connected event triggers mdoc session Connected`() = runTest {
        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
            Assert.assertEquals(
                MdocSessionState.Connected(DEVICE_ADDRESS),
                awaitItem()
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `gatt service added event triggers mdoc session service added`() = runTest {
        val service = mockk<BluetoothGattService>()
        every { service.uuid } returns uuid

        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(
                GattServerEvent.ServiceAdded(
                    BluetoothGatt.GATT_SUCCESS,
                    service
                )
            )
            Assert.assertEquals(
                MdocSessionState.ServiceAdded(service.uuid),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt Disconnected event riggers mdoc session Disconnected`() = runTest {
        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
            Assert.assertEquals(
                MdocSessionState.Connected(DEVICE_ADDRESS),
                awaitItem()
            )

            gattServerManager.emitEvent(GattServerEvent.Disconnected(DEVICE_ADDRESS))
            Assert.assertEquals(
                MdocSessionState.Disconnected(DEVICE_ADDRESS),
                awaitItem()
            )
        }
    }

    @Test
    fun `duplicate gatt Connected for same device does not emit duplicate Connected state`() =
        runTest {
            sessionManager.state.test {
                Assert.assertEquals(MdocSessionState.Idle, awaitItem())

                gattServerManager.emitEvent(GattServerEvent.Connected(DEVICE_ADDRESS))
                Assert.assertEquals(
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
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(GattServerEvent.Disconnected(DEVICE_ADDRESS))

            expectNoEvents()
        }
    }

    @Test
    fun `gatt Error event maps to session Error state`() = runTest {
        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

            gattServerManager.emitEvent(
                GattServerEvent.Error(
                    MdocSessionError.GATT_NOT_AVAILABLE
                )
            )
            Assert.assertEquals(
                MdocSessionState.Error(MdocSessionError.GATT_NOT_AVAILABLE),
                awaitItem()
            )
        }
    }

    @Test
    fun `gatt UnsupportedEvent does not change session state`() = runTest {
        sessionManager.state.test {
            Assert.assertEquals(MdocSessionState.Idle, awaitItem())

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
}
