package uk.gov.onelogin.sharing.bluetooth

import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.BluetoothLeAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingFailureReason
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.ble.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.ble.toAndroid
import uk.gov.onelogin.sharing.bluetooth.ble.toReason

class AndroidBluetoothAdvertiserProvider(private val bluetoothAdapter: BluetoothAdapterProvider) :
    BluetoothAdvertiserProvider {
    private var currentCallback: AdvertisingSetCallback? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var callback: AdvertisingCallback? = null

    override fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        advertiser = bluetoothAdapter.getAdvertiser()
        this.callback = callback

        if (currentCallback != null) {
            callback.onAdvertisingStartFailed(AdvertisingFailureReason.ALREADY_STARTED)
            return
        }

        if (advertiser == null) {
            callback.onAdvertisingStartFailed(AdvertisingFailureReason.ADVERTISER_NULL)
            return
        }

        currentCallback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet?,
                txPower: Int,
                status: Int
            ) {
                if (status == ADVERTISE_SUCCESS) {
                    callback.onAdvertisingStarted()
                } else {
                    callback.onAdvertisingStartFailed(status.toReason())
                    currentCallback = null
                }
            }

            override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                callback.onAdvertisingStopped()
                currentCallback = null
            }
        }

        try {
            advertiser?.startAdvertisingSet(
                parameters.toAndroid(),
                bleAdvertiseData.toAndroid(),
                null,
                null,
                null,
                currentCallback
            )
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    override fun stopAdvertisingSet() {
        try {
            advertiser?.stopAdvertisingSet(currentCallback)
        } catch (e: SecurityException) {
            callback?.onAdvertisingStartFailed(
                AdvertisingFailureReason.ADVERTISE_FAILED_SECURITY_EXCEPTION
            )
            println(e.message ?: "Security exception")
        } finally {
            currentCallback = null
            advertiser = null
        }
    }
}
