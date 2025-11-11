package uk.gov.onelogin.sharing.bluetooth

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
import android.os.ParcelUuid
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingCallback
import uk.gov.onelogin.sharing.bluetooth.ble.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.ble.BleAdvertiseData
import uk.gov.onelogin.sharing.bluetooth.ble.Status

class AndroidBluetoothAdvertiserProvider(private val bluetoothAdapter: BluetoothAdapterProvider) :
    BluetoothAdvertiserProvider {
    private var currentCallback: AdvertisingSetCallback? = null

    override fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        if (currentCallback != null) {
            callback.onAdvertisingFailed(Status.AlreadyStarted)
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
                }
            }

            override fun onAdvertisingSetStopped(advertisingSet: AdvertisingSet?) {
                callback.onAdvertisingStopped()
            }
        }

        val advertisingParameters = AdvertisingSetParameters.Builder()
            .setLegacyMode(parameters.legacyMode)
            .setInterval(parameters.interval)
            .setTxPowerLevel(parameters.txPowerLevel)
            .setPrimaryPhy(parameters.primaryPhy)
            .setSecondaryPhy(parameters.secondaryPhy)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(bleAdvertiseData.serviceUuid))
            .addServiceData(
                ParcelUuid(bleAdvertiseData.serviceUuid),
                bleAdvertiseData.payload.asBytes()
            )
            .build()

        try {
            bluetoothAdapter.getAdvertiser()?.startAdvertisingSet(
                advertisingParameters,
                data,
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
            bluetoothAdapter.getAdvertiser()?.stopAdvertisingSet(currentCallback)
        } catch (e: SecurityException) {
            println(e.message ?: "Security exception")
        } finally {
            currentCallback = null
        }
    }
}
