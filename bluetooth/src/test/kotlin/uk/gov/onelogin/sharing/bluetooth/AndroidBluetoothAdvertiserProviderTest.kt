package uk.gov.onelogin.sharing.bluetooth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallbackStub
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.ble.Status
import uk.gov.onelogin.sharing.bluetooth.ble.stubBleAdvertiseData

@RunWith(RobolectricTestRunner::class)
class AndroidBluetoothAdvertiserProviderTest {

    @Test
    fun `second start fails with AlreadyStarted`() {
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

        provider.startAdvertisingSet(
            AdvertisingParameters(),
            stubBleAdvertiseData(),
            callback2
        )
        assertEquals(Status.AlreadyStarted, callback2.failed)
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

        assertNotEquals(Status.AlreadyStarted, callback2.failed)
    }
}
