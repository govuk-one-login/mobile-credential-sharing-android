package uk.gov.onelogin.sharing.bluetooth.ble

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertisingSet
import android.bluetooth.le.AdvertisingSetCallback
import android.bluetooth.le.AdvertisingSetParameters
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat

class AndroidBleProvider(val context: Context) : BleProvider {
    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val bluetoothAdapter get() = bluetoothManager.adapter
    private var bleAdvertiser: BluetoothLeAdvertiser? = null
    private var advertisingSetCallback: AdvertisingSetCallback? = null

    override fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    override fun hasAdvertisePermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ) == PackageManager.PERMISSION_GRANTED

    override fun startAdvertisingSet(
        parameters: AdvertisingParameters,
        bleAdvertiseData: BleAdvertiseData,
        callback: AdvertisingCallback
    ) {
        bleAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
        check(bleAdvertiser != null) {
            "Bluetooth advertiser not available"
        }

        advertisingSetCallback = object : AdvertisingSetCallback() {
            override fun onAdvertisingSetStarted(
                advertisingSet: AdvertisingSet?,
                txPower: Int,
                status: Int
            ) {
                if (status == ADVERTISE_SUCCESS) {
                    callback.onAdvertisingStarted()
                } else {
                    callback.onAdvertisingFailed(status)
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
            bleAdvertiser?.startAdvertisingSet(
                advertisingParameters,
                data,
                null,
                null,
                null,
                this.advertisingSetCallback
            )
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }

    override fun stopAdvertisingSet(callback: AdvertisingCallback?) {
        try {
            bleAdvertiser?.stopAdvertisingSet(this.advertisingSetCallback)
        } catch (e: SecurityException) {
            println(e.message)
        }
    }
}
