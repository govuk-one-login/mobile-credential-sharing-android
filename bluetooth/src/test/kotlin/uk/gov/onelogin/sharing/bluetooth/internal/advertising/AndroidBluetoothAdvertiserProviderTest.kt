package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.BluetoothLeAdvertiser
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallbackStub
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.ble.stubBleAdvertiseData

@RunWith(RobolectricTestRunner::class)
internal class AndroidBluetoothAdvertiserProviderTest {
    private val advertiser = mockk<BluetoothLeAdvertiser>(relaxed = true)
    private val logger = SystemLogger()
    private val adapter = FakeBluetoothAdapterProvider(
        isEnabled = true,
        advertiser = advertiser
    )
    private val provider = AndroidBluetoothAdvertiserProvider(
        bluetoothAdapter = adapter,
        logger = logger
    )
    private val callbackSlot = slot<AdvertisingSetCallback>()
    private val callback = AdvertisingCallbackStub()

    @Test
    fun `second start fails with AlreadyStarted`() {
        val callback1 = AdvertisingCallbackStub()
        val callback2 = AdvertisingCallbackStub()

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback1
        )

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback2
        )

        Assert.assertEquals(
            AdvertisingFailureReason.ALREADY_STARTED,
            callback2.advertisingFailureReason
        )
    }

    @Test
    fun `stop clears internal callback`() {
        val provider = AndroidBluetoothAdvertiserProvider(
            bluetoothAdapter = FakeBluetoothAdapterProvider(isEnabled = true),
            logger = logger
        )
        val callback1 = AdvertisingCallbackStub()
        val callback2 = AdvertisingCallbackStub()

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback1
        )

        provider.stopAdvertisingSet()

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback2
        )

        Assert.assertNotEquals(
            AdvertisingFailureReason.ALREADY_STARTED,
            callback2.advertisingFailureReason
        )
    }

    @Test
    fun `null advertiser returns internal error`() {
        val callback = AdvertisingCallbackStub()

        val adapter = FakeBluetoothAdapterProvider(
            isEnabled = true,
            advertiser = null
        )

        val provider = AndroidBluetoothAdvertiserProvider(
            bluetoothAdapter = adapter,
            logger = logger
        )

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback
        )

        Assert.assertEquals(
            AdvertisingFailureReason.ADVERTISER_NULL,
            callback.advertisingFailureReason
        )
    }

    @Test
    fun `onAdvertisingStarted callback triggered when advertising is started`() {
        startAdvertising()

        callbackSlot.captured.onAdvertisingSetStarted(
            null,
            0,
            AdvertisingSetCallback.ADVERTISE_SUCCESS
        )

        Assert.assertTrue(callback.started)
    }

    @Test
    fun `onAdvertisingStopped callback triggered when advertising is stopped`() {
        startAdvertising()

        callbackSlot.captured.onAdvertisingSetStarted(
            null,
            0,
            AdvertisingSetCallback.ADVERTISE_SUCCESS
        )

        callbackSlot.captured.onAdvertisingSetStopped(null)

        Assert.assertTrue(callback.stopped)
    }

    @Test
    fun `maps non-success status to onAdvertisingStarted`() {
        startAdvertising()

        val status = AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS
        callbackSlot.captured.onAdvertisingSetStarted(
            null,
            0,
            status
        )

        Assert.assertEquals(status.toReason(), callback.advertisingFailureReason)
    }

    @Test
    fun `stop forwards to advertiser and clears callback`() {
        startAdvertising()

        provider.stopAdvertisingSet()

        verify { advertiser.stopAdvertisingSet(callbackSlot.captured) }
    }

    @Test
    fun `stop surfaces security exception as failure`() {
        every {
            advertiser.stopAdvertisingSet(any())
        } throws SecurityException()

        startAdvertising()

        provider.stopAdvertisingSet()

        Assert.assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_SECURITY_EXCEPTION,
            callback.advertisingFailureReason
        )

        assert(logger.contains("Security exception"))
    }

    private fun startAdvertising() {
        every {
            advertiser.startAdvertisingSet(
                any(),
                any(),
                any(),
                any(),
                any(),
                capture(callbackSlot)
            )
        } just Runs

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback
        )
    }

    @Test
    fun `illegal argument from advertiser maps to internal error`() {
        every {
            advertiser.startAdvertisingSet(
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } throws IllegalArgumentException("bad args")

        val callback = AdvertisingCallbackStub()

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback
        )

        Assert.assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR,
            callback.advertisingFailureReason
        )

        assert(logger.contains("bad args"))
    }
}
