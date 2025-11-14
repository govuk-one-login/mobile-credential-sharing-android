package uk.gov.onelogin.sharing.bluetooth.api

import app.cash.turbine.test
import java.util.UUID
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleProvider
import uk.gov.onelogin.sharing.bluetooth.ble.stubBleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.util.MainDispatcherRule
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

        TestCase.assertEquals(
            AdvertiserStartResult.Error("Bluetooth is disabled"),
            result
        )
    }

    @Test
    fun `start fails when invalid UUID`() = runTest {
        val result = bleAdvertiser.startAdvertise(
            stubBleAdvertiseData(
                UUID.fromString("00000000-0000-0000-0000-000000000000")
            )
        )

        TestCase.assertEquals(
            AdvertiserStartResult.Error("Invalid UUID"),
            result
        )
    }

    @Test
    fun `start fails when permission not granted`() = runTest {
        permissionChecker.hasPermission = false
        val result = bleAdvertiser.startAdvertise(
            stubBleAdvertiseData()
        )

        TestCase.assertEquals(
            AdvertiserStartResult.Error("Missing permissions"),
            result
        )
    }

    @Test
    fun `start fails when exception thrown`() = runTest {
        bleProvider.thrownOnStart = IllegalStateException("")
        val result = bleAdvertiser.startAdvertise(
            stubBleAdvertiseData()
        )

        TestCase.assertEquals(
            AdvertiserStartResult.Error("Failed to start advertising"),
            result
        )
    }

    @Test
    fun `success state events triggered when advertising is started`() = runTest {
        bleAdvertiser.state.test {
            TestCase.assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            TestCase.assertEquals(AdvertiserState.Starting, awaitItem())

            // Bluetooth advertising service has started successfully
            bleProvider.triggerOnAdvertisingStarted()

            TestCase.assertEquals(AdvertiserState.Started, awaitItem())

            TestCase.assertEquals(
                AdvertiserStartResult.Success,
                deferredStart.await()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `returns error when start advertising called and state is starting`() = runTest {
        bleAdvertiser.state.test {
            TestCase.assertEquals(AdvertiserState.Idle, awaitItem())

            val first = async {
                bleAdvertiser.startAdvertise(
                    stubBleAdvertiseData()
                )
            }

            TestCase.assertEquals(AdvertiserState.Starting, awaitItem())

            val second = bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            TestCase.assertEquals(
                AdvertiserStartResult.Error("Already starting"),
                second
            )

            first.cancelAndJoin()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `returns error when start advertising called and state is started`() = runTest {
        bleAdvertiser.state.test {
            TestCase.assertEquals(AdvertiserState.Idle, awaitItem())

            val first = async {
                bleAdvertiser.startAdvertise(
                    stubBleAdvertiseData()
                )
            }

            TestCase.assertEquals(AdvertiserState.Starting, awaitItem())

            bleProvider.triggerOnAdvertisingStarted()
            TestCase.assertEquals(AdvertiserState.Started, awaitItem())

            val second = bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            TestCase.assertEquals(
                AdvertiserStartResult.Error("Already starting"),
                second
            )

            first.cancelAndJoin()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `failed state events triggered when advertising fails`() = runTest {
        val reason = AdvertisingFailureReason.ADVERTISER_NULL

        bleAdvertiser.state.test {
            TestCase.assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            TestCase.assertEquals(AdvertiserState.Starting, awaitItem())

            // Bluetooth advertising service has failed to start
            bleProvider.triggerOnAdvertisingFailed(reason)

            TestCase.assertEquals(
                AdvertiserState.Failed("start failed: $reason"),
                awaitItem()
            )

            TestCase.assertEquals(
                AdvertiserStartResult.Error("Failed to start advertising"),
                deferredStart.await()
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stopped event triggered when advertising is stopped by the service`() = runTest {
        bleAdvertiser.state.test {
            TestCase.assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            TestCase.assertEquals(AdvertiserState.Starting, awaitItem())

            // Bluetooth advertising service has started successfully
            bleProvider.triggerOnAdvertisingStarted()

            TestCase.assertEquals(AdvertiserState.Started, awaitItem())

            TestCase.assertEquals(
                AdvertiserStartResult.Success,
                deferredStart.await()
            )

            // Bluetooth advertising service has stopped unexpectedly
            bleProvider.triggerOnAdvertisingStopped()

            TestCase.assertEquals(AdvertiserState.Stopped, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `stop advertising is successful after successful start`() = runTest {
        bleAdvertiser.state.test {
            TestCase.assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            TestCase.assertEquals(AdvertiserState.Starting, awaitItem())

            // Bluetooth advertising service has started successfully
            bleProvider.triggerOnAdvertisingStarted()

            TestCase.assertEquals(AdvertiserState.Started, awaitItem())

            TestCase.assertEquals(
                AdvertiserStartResult.Success,
                deferredStart.await()
            )

            bleAdvertiser.stopAdvertise()

            TestCase.assertEquals(AdvertiserState.Stopping, awaitItem())
            TestCase.assertEquals(AdvertiserState.Stopped, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `cancel during start calls stop Advertising`() = runTest {
        bleAdvertiser.state.test {
            TestCase.assertEquals(AdvertiserState.Idle, awaitItem())

            val startJob = async { bleAdvertiser.startAdvertise(stubBleAdvertiseData()) }

            TestCase.assertEquals(AdvertiserState.Starting, awaitItem())

            startJob.cancelAndJoin()

            TestCase.assertEquals(AdvertiserState.Stopping, awaitItem())
            TestCase.assertEquals(AdvertiserState.Stopped, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `start returns Error on timeout when provider never starts`() = runTest {
        val timedAdvertiser = AndroidBleAdvertiser(
            bleProvider = bleProvider,
            permissionChecker = permissionChecker,
            startTimeoutMs = 1_000L
        )

        timedAdvertiser.state.test {
            TestCase.assertEquals(AdvertiserState.Idle, awaitItem())

            val deferred = async {
                timedAdvertiser.startAdvertise(
                    stubBleAdvertiseData()
                )
            }

            TestCase.assertEquals(AdvertiserState.Starting, awaitItem())

            advanceTimeBy(1_050L)

            val result = deferred.await()

            TestCase.assertEquals(
                AdvertiserStartResult.Error("Advertising start timed out"),
                result
            )

            cancelAndIgnoreRemainingEvents()
        }
    }
}
