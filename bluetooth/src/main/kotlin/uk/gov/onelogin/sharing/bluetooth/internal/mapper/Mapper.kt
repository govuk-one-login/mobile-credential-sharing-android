package uk.gov.onelogin.sharing.bluetooth.internal.mapper

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertisingSetParameters
import android.os.ParcelUuid
import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData

/**
 * Converts this to the Android-specific advertising data.
 */
internal object BleAdvertiseDataMapper {
    fun toAndroid(data: BleAdvertiseData): AdvertiseData = AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(data.serviceUuid))
        .build()
}

/**
 * Converts this to the Android-specific parameters.
 */
internal object AdvertisingParametersMapper {
    fun toAndroid(parameters: AdvertisingParameters): AdvertisingSetParameters =
        AdvertisingSetParameters.Builder()
            .setLegacyMode(parameters.legacyMode)
            .setInterval(parameters.interval)
            .setTxPowerLevel(parameters.txPowerLevel)
            .setConnectable(parameters.connectable)
            .build()
}
