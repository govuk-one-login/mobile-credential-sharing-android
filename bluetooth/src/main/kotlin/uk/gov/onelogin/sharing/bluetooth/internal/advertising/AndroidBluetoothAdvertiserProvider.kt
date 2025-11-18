package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.BluetoothLeAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingFailureReason
import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.api.toReason
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.internal.mapper.AdvertisingParametersMapper
import uk.gov.onelogin.sharing.bluetooth.internal.mapper.BleAdvertiseDataMapper

internal class AndroidBluetoothAdvertiserProvider(
    private val bluetoothAdapter: BluetoothAdapterProvider
) : BluetoothAdvertiserProvider {
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

        currentCallback = AndroidAdvertisingSetCallback(
            callback = callback,
            onClearCallback = { currentCallback = null }
        )

        try {
            advertiser?.startAdvertisingSet(
                AdvertisingParametersMapper.toAndroid(parameters),
                BleAdvertiseDataMapper.toAndroid(bleAdvertiseData),
                null,
                null,
                null,
                currentCallback
            )
        } catch (e: IllegalArgumentException) {
            println(e.message)
            callback.onAdvertisingStartFailed(
                AdvertisingFailureReason.ADVERTISE_FAILED_INTERNAL_ERROR
            )
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

internal class AndroidAdvertisingSetCallback(
    private val callback: AdvertisingCallback,
    private val onClearCallback: () -> Unit

) : AdvertisingSetCallback() {
    override fun onAdvertisingSetStarted(
        advertisingSet: AdvertisingSet?,
        txPower: Int,
        status: Int
    ) {
        if (status == ADVERTISE_SUCCESS) {
            callback.onAdvertisingStarted()
        } else {
            callback.onAdvertisingStartFailed(status.toReason())
            onClearCallback()
        }
    }

    override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
        callback.onAdvertisingStopped()
        onClearCallback()
    }
}
