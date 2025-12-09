package uk.gov.onelogin.sharing.bluetooth.internal.advertising

import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.BluetoothLeAdvertiser
import uk.gov.logging.api.Logger
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.internal.mapper.AdvertisingParametersMapper
import uk.gov.onelogin.sharing.bluetooth.internal.mapper.BleAdvertiseDataMapper
import uk.gov.onelogin.sharing.core.logger.logTag

internal class AndroidBluetoothAdvertiserProvider(
    private val bluetoothAdapter: BluetoothAdapterProvider,
    private val logger: Logger
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
            logger.error(logTag, e.message ?: "Illegal argument", e)
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
            logger.error(logTag, e.message ?: "Security exception", e)
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
