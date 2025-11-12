package uk.gov.onelogin.sharing.bluetooth.advertiser

import app.cash.turbine.test
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.MainDispatcherRule
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleProvider
import uk.gov.onelogin.sharing.bluetooth.ble.Reason
import uk.gov.onelogin.sharing.bluetooth.ble.stubBleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.permissions.FakePermissionChecker

class AndroidBleAdvertiserTest {
    private lateinit var bleProvider: FakeBleProvider
    private lateinit var bleAdvertiser: AndroidBleAdvertiser
    private val permissionChecker = FakePermissionChecker()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        bleProvider = FakeBleProvider()
        bleAdvertiser = AndroidBleAdvertiser(
            bleProvider,
            permissionChecker
        )
    }

    @Test
    fun `is bluetooth enabled returns true when provider is enabled`() {
        bleProvider.enabled = true
        assert(bleAdvertiser.isBluetoothEnabled())
    }

    @Test
    fun `is bluetooth enabled returns false when provider is disabled`() {
        bleProvider.enabled = false
        assert(!bleAdvertiser.isBluetoothEnabled())
    }

    @Test
    fun `has advertise permission returns true when provider has permissions`() {
        assert(bleAdvertiser.hasAdvertisePermission())
    }

    @Test
    fun `has advertise permission returns false when provider does not have permissions`() {
        permissionChecker.hasPermission = false
        assert(!bleAdvertiser.hasAdvertisePermission())
    }

    @Test
    fun `start fails when bluetooth is not enabled`() = runTest {
        bleProvider.enabled = false
        val result = bleAdvertiser.startAdvertise(
            stubBleAdvertiseData()
        )

        assertEquals(
            AdvertiserStartResult.Error("Bluetooth is disabled"),
            result
        )
    }

    @Test
    fun `start fails when permission not granted`() = runTest {
        permissionChecker.hasPermission = false
        val result = bleAdvertiser.startAdvertise(
            stubBleAdvertiseData()
        )

        assertEquals(
            AdvertiserStartResult.Error("Missing permissions"),
            result
        )
    }

    @Test
    fun `start fails when exception thrown`() = runTest {
        bleProvider.thrownOnStart = IllegalStateException("Test exception")
        val result = bleAdvertiser.startAdvertise(
            stubBleAdvertiseData()
        )

        assertEquals(
            AdvertiserStartResult.Error("Test exception"),
            result
        )
    }

    @Test
    fun `success state events triggered when advertising is started`() = runTest {
        bleAdvertiser.state.test {
            assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            assertEquals(AdvertiserState.Starting, awaitItem())

            // Bluetooth advertising service has started successfully
            bleProvider.triggerOnAdvertisingStarted()

            assertEquals(AdvertiserState.Started, awaitItem())

            assertEquals(
                AdvertiserStartResult.Success,
                deferredStart.await()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed state events triggered when advertising fails`() = runTest {
        val failReason = Reason.ADVERTISER_NULL

        bleAdvertiser.state.test {
            assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            assertEquals(AdvertiserState.Starting, awaitItem())

            // Bluetooth advertising service has failed to start
            bleProvider.triggerOnAdvertisingFailed(failReason)

            assertEquals(
                AdvertiserState.Failed("start failed: $failReason"),
                awaitItem()
            )

            assertEquals(
                AdvertiserStartResult.Error("start failed: $failReason"),
                deferredStart.await()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stopped event triggered when advertising is stopped by the service`() = runTest {
        bleAdvertiser.state.test {
            assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            assertEquals(AdvertiserState.Starting, awaitItem())

            // Bluetooth advertising service has started successfully
            bleProvider.triggerOnAdvertisingStarted()

            assertEquals(AdvertiserState.Started, awaitItem())

            assertEquals(
                AdvertiserStartResult.Success,
                deferredStart.await()
            )

            // Bluetooth advertising service has stopped unexpectedly
            bleProvider.triggerOnAdvertisingStopped()

            assertEquals(AdvertiserState.Stopped, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `stop advertising is successful after successful start`() = runTest {
        bleAdvertiser.state.test {
            assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            assertEquals(AdvertiserState.Starting, awaitItem())

            // Bluetooth advertising service has started successfully
            bleProvider.triggerOnAdvertisingStarted()

            assertEquals(AdvertiserState.Started, awaitItem())

            assertEquals(
                AdvertiserStartResult.Success,
                deferredStart.await()
            )

            bleAdvertiser.stopAdvertise()

            assertEquals(AdvertiserState.Stopping, awaitItem())
            assertEquals(AdvertiserState.Stopped, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancel during start calls stop Advertising`() = runTest {
        bleAdvertiser.state.test {
            assertEquals(AdvertiserState.Idle, awaitItem())

            val startJob = async { bleAdvertiser.startAdvertise(stubBleAdvertiseData()) }

            assertEquals(AdvertiserState.Starting, awaitItem())

            startJob.cancelAndJoin()

            assertEquals(AdvertiserState.Stopping, awaitItem())
            assertEquals(AdvertiserState.Stopped, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
