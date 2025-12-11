package uk.gov.onelogin.sharing.bluetooth.api.advertising

import android.bluetooth.le.AdvertisingSetParameters.INTERVAL_HIGH
import android.bluetooth.le.AdvertisingSetParameters.TX_POWER_MEDIUM

/**
 * Holds the parameters for Bluetooth Low Energy (BLE) advertising.
 *
 * This class encapsulates various settings that control how the BLE advertising is performed
 * It provides a platform-agnostic way to configure these parameters.
 *
 * @param legacyMode Whether to use legacy advertising mode. Defaults to `false`.
 * @param interval The advertising interval. Defaults to [INTERVAL_HIGH].
 * @param txPowerLevel The transmission power level. Defaults to
 * [TX_POWER_MEDIUM].
 * @param connectable Whether the advertising is connectable. Defaults to `true`.
 */
data class AdvertisingParameters(
    val legacyMode: Boolean = false,
    val interval: Int = INTERVAL_HIGH,
    val txPowerLevel: Int = TX_POWER_MEDIUM,
    val connectable: Boolean = true
)
