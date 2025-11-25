package uk.gov.onelogin.sharing.bluetooth.ble

import uk.gov.onelogin.sharing.bluetooth.api.permissions.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingFailureReason
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.internal.core.BleProvider

class FakeBleProvider : BleProvider {
    var enabled = true
    var thrownOnStart: Throwable? = null
    var parameters: AdvertisingParameters? = null
    var data: BleAdvertiseData? = null
    var callback: AdvertisingCallback? = null

    override fun isBluetoothEnabled(): Boolean = enabled

    override fun startAdvertising(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        thrownOnStart?.let { throw it }
        this.parameters = parameters
        this.data = bleAdvertiseData
        this.callback = callback
    }

    override fun stopAdvertising() {
        requireNotNull(callback) { "Must call startAdvertising first" }
        callback?.onAdvertisingStopped()
    }

    fun triggerOnAdvertisingStarted() {
        requireNotNull(callback) { "Must call startAdvertising first" }
        callback?.onAdvertisingStarted()
    }

    fun triggerOnAdvertisingFailed(reason: AdvertisingFailureReason) {
        requireNotNull(callback) { "Must call startAdvertising first" }
        callback?.onAdvertisingStartFailed(reason)
    }

    fun triggerOnAdvertisingStopped() {
        requireNotNull(callback) { "Must call startAdvertising first" }
        callback?.onAdvertisingStopped()
    }
}
