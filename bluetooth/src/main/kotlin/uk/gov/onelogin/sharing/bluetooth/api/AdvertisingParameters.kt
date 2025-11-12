package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertisingSetParameters

data class AdvertisingParameters(
    val legacyMode: Boolean = false,
    val interval: Int = AdvertisingSetParameters.INTERVAL_HIGH,
    val txPowerLevel: Int = AdvertisingSetParameters.TX_POWER_MEDIUM,
    val primaryPhy: Int = BluetoothDevice.PHY_LE_1M,
    val secondaryPhy: Int = BluetoothDevice.PHY_LE_2M
)

fun AdvertisingParameters.toAndroid(): AdvertisingSetParameters = AdvertisingSetParameters.Builder()
    .setLegacyMode(legacyMode)
    .setInterval(interval)
    .setTxPowerLevel(txPowerLevel)
    .setPrimaryPhy(primaryPhy)
    .setSecondaryPhy(secondaryPhy)
    .build()
