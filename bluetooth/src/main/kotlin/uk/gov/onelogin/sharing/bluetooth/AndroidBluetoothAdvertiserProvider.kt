package uk.gov.onelogin.sharing.bluetooth

import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.BluetoothLeAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.ADVERTISE_FAILED_INTERNAL_ERROR
import uk.gov.onelogin.sharing.bluetooth.ble.ADVERTISE_FAILED_SECURITY_EXCEPTION
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.ble.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.ble.Status
import uk.gov.onelogin.sharing.bluetooth.ble.toAndroid

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
            callback.onAdvertisingFailed(Status.AlreadyStarted)
            return
        }

        if (advertiser == null) {
            callback.onAdvertisingFailed(Status.Error(ADVERTISE_FAILED_INTERNAL_ERROR))
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
                    callback.onAdvertisingFailed(Status.Error(status))
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
            callback?.onAdvertisingFailed(
                Status.Error(
                    ADVERTISE_FAILED_SECURITY_EXCEPTION
                )
            )
            println(e.message ?: "Security exception")
        } finally {
            currentCallback = null
            advertiser = null
        }
    }
}
