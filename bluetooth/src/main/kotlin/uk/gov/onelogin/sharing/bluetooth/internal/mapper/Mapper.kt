package uk.gov.onelogin.sharing.bluetooth.internal.mapper

import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertisingSetParameters
import android.os.ParcelUuid
import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingParameters
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiseData

/**
 * Converts this to the Android-specific advertising data.
 */
fun BleAdvertiseData.toAndroid(): AdvertiseData? {
    println("advertising UUID: $serviceUuid")
    return AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(serviceUuid))
        .build()
}

/**
 * Converts this to the Android-specific parameters.
 */
fun AdvertisingParameters.toAndroid(): AdvertisingSetParameters = AdvertisingSetParameters.Builder()
    .setLegacyMode(legacyMode)
    .setInterval(interval)
    .setTxPowerLevel(txPowerLevel)
    .setConnectable(connectable)
    .build()
