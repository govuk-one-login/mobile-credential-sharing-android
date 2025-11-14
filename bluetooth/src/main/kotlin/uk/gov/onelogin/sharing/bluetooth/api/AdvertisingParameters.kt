package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.le.AdvertisingSetParameters

/**
 * Holds the parameters for Bluetooth Low Energy (BLE) advertising.
 *
 * This class encapsulates various settings that control how the BLE advertising is performed
 * It provides a platform-agnostic way to configure these parameters.
 * The toAndroid() converts this to the Android-specific parameters.
 *
 * @param legacyMode Whether to use legacy advertising mode. Defaults to `false`.
 * @param interval The advertising interval. Defaults to [AdvertisingSetParameters.INTERVAL_HIGH].
 * @param txPowerLevel The transmission power level. Defaults to [AdvertisingSetParameters.TX_POWER_MEDIUM].
 * @param connectable Whether the advertising is connectable. Defaults to `true`.
 */
data class AdvertisingParameters(
    val legacyMode: Boolean = false,
    val interval: Int = AdvertisingSetParameters.INTERVAL_HIGH,
    val txPowerLevel: Int = AdvertisingSetParameters.TX_POWER_MEDIUM,
    val connectable: Boolean = true
)

fun AdvertisingParameters.toAndroid(): AdvertisingSetParameters = AdvertisingSetParameters.Builder()
    .setLegacyMode(legacyMode)
    .setInterval(interval)
    .setTxPowerLevel(txPowerLevel)
    .setConnectable(connectable)
    .build()
