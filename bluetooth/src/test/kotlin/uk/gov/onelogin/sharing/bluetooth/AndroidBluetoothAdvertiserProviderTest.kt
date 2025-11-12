package uk.gov.onelogin.sharing.bluetooth

import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS
import android.bluetooth.le.AdvertisingSetCallback.ADVERTISE_SUCCESS
import android.bluetooth.le.BluetoothLeAdvertiser
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallbackStub
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingFailureReason
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.ble.stubBleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.ble.toReason

@RunWith(RobolectricTestRunner::class)
class AndroidBluetoothAdvertiserProviderTest {
    private val advertiser = mockk<BluetoothLeAdvertiser>(relaxed = true)
    private val adapter = FakeBluetoothAdapterProvider(
        isEnabled = true,
        advertiser = advertiser
    )
    private val provider = AndroidBluetoothAdvertiserProvider(adapter)
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

        assertEquals(
            AdvertisingFailureReason.ALREADY_STARTED,
            callback2.advertisingFailureReason
        )
    }

    @Test
    fun `stop clears internal callback`() {
        val provider = AndroidBluetoothAdvertiserProvider(
            FakeBluetoothAdapterProvider(true)
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

        assertNotEquals(
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

        val provider = AndroidBluetoothAdvertiserProvider(adapter)

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback
        )

        assertEquals(
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
            ADVERTISE_SUCCESS
        )

        assertTrue(callback.started)
    }

    @Test
    fun `onAdvertisingStopped callback triggered when advertising is stopped`() {
        startAdvertising()

        callbackSlot.captured.onAdvertisingSetStarted(
            null,
            0,
            ADVERTISE_SUCCESS
        )

        callbackSlot.captured.onAdvertisingSetStopped(null)

        assertTrue(callback.stopped)
    }

    @Test
    fun `maps non-success status to onAdvertisingStarted`() {
        startAdvertising()

        val status = ADVERTISE_FAILED_TOO_MANY_ADVERTISERS
        callbackSlot.captured.onAdvertisingSetStarted(
            null,
            0,
            status
        )

        assertEquals(status.toReason(), callback.advertisingFailureReason)
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

        assertEquals(
            AdvertisingFailureReason.ADVERTISE_FAILED_SECURITY_EXCEPTION,
            callback.advertisingFailureReason
        )
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
}
