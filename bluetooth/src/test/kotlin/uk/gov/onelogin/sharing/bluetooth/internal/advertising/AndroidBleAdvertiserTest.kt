package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import app.cash.turbine.test
import java.util.UUID
import kotlin.test.assertFailsWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleProvider
import uk.gov.onelogin.sharing.bluetooth.ble.stubBleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.util.MainDispatcherRule
import uk.gov.onelogin.sharing.bluetooth.permissions.FakePermissionChecker

@OptIn(ExperimentalCoroutinesApi::class)
internal class AndroidBleAdvertiserTest {
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

        val exception = assertFailsWith<StartAdvertisingException> {
            bleAdvertiser.startAdvertise(
                stubBleAdvertiseData()
            )
        }

        assertEquals(
            "Error: ${AdvertisingError.BLUETOOTH_DISABLED}",
            exception.message
        )
    }

    @Test
    fun `start fails when invalid UUID`() = runTest {
        val exception = assertFailsWith<StartAdvertisingException> {
            bleAdvertiser.startAdvertise(
                stubBleAdvertiseData(
                    UUID.fromString("00000000-0000-0000-0000-000000000000")
                )
            )
        }

        assertEquals(
            "Error: ${AdvertisingError.INVALID_UUID}",
            exception.message
        )
    }

    @Test
    fun `start fails when permission not granted`() = runTest {
        permissionChecker.hasPermission = false

        val exception = assertFailsWith<StartAdvertisingException> {
            bleAdvertiser.startAdvertise(
                stubBleAdvertiseData()
            )
        }

        assertEquals(
            "Error: ${AdvertisingError.MISSING_PERMISSION}",
            exception.message
        )
    }

    @Test
    fun `start fails when exception thrown`() = runTest {
        bleProvider.thrownOnStart = StartAdvertisingException(AdvertisingError.INTERNAL_ERROR)

        val exception = assertFailsWith<StartAdvertisingException> {
            bleAdvertiser.startAdvertise(stubBleAdvertiseData())
        }

        assertEquals(
            "Error: ${AdvertisingError.INTERNAL_ERROR}",
            exception.message
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

            deferredStart.await()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `throws exception when called while state is Starting`() = runTest {
        val firstStart = async {
            bleAdvertiser.startAdvertise(stubBleAdvertiseData())
        }

        runCurrent()

        assertEquals(AdvertiserState.Starting, bleAdvertiser.state.value)

        val exception = assertFailsWith<StartAdvertisingException> {
            bleAdvertiser.startAdvertise(stubBleAdvertiseData())
        }

        assertEquals(AdvertisingError.ALREADY_IN_PROGRESS, exception.error)

        firstStart.cancelAndJoin()
    }

    @Test
    fun `throws exception when start advertising called and state is started`() = runTest {
        val firstStart = async {
            bleAdvertiser.startAdvertise(stubBleAdvertiseData())
        }

        runCurrent()

        bleProvider.triggerOnAdvertisingStarted()

        firstStart.await()

        assertEquals(AdvertiserState.Started, bleAdvertiser.state.value)

        val exception = assertFailsWith<StartAdvertisingException> {
            bleAdvertiser.startAdvertise(stubBleAdvertiseData())
        }

        assertEquals(AdvertisingError.ALREADY_IN_PROGRESS, exception.error)

        firstStart.cancelAndJoin()
    }

    @Test
    fun `failed state events triggered when advertising fails`() = runTest {
        val reason = AdvertisingFailureReason.ADVERTISER_NULL

        bleAdvertiser.state.test {
            assertEquals(AdvertiserState.Idle, awaitItem())

            val deferredStart = async {
                bleAdvertiser.startAdvertise(stubBleAdvertiseData())
            }

            runCurrent()

            assertEquals(AdvertiserState.Starting, awaitItem())

            bleProvider.triggerOnAdvertisingFailed(reason)

            assertEquals(
                AdvertiserState.Failed("start failed: $reason"),
                awaitItem()
            )

            deferredStart.await()

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

            deferredStart.await()

            // Bluetooth advertising service has stopped unexpectedly
            bleProvider.triggerOnAdvertisingStopped()

            assertEquals(AdvertiserState.Stopped, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

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

            deferredStart.await()

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

    @Test
    fun `start throws exception on timeout when provider never starts`() = runTest {
        val timedAdvertiser = AndroidBleAdvertiser(
            bleProvider = bleProvider,
            permissionChecker = permissionChecker,
            startTimeoutMs = 1_000L
        )

        val exception = assertFailsWith<StartAdvertisingException> {
            timedAdvertiser.startAdvertise(stubBleAdvertiseData())
        }

        assertEquals(AdvertisingError.START_TIMEOUT, exception.error)
    }
}
