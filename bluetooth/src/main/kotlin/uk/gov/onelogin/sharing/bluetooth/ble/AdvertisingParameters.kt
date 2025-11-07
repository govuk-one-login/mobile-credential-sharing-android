package uk.gov.onelogin.sharing.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertisingSetParameters

data class AdvertisingParameters(
    val legacyMode: Boolean = false,
    val interval: Int = AdvertisingSetParameters.INTERVAL_HIGH,
    val txPowerLevel: Int = AdvertisingSetParameters.TX_POWER_MEDIUM,
    val primaryPhy: Int = BluetoothDevice.PHY_LE_1M,
    val secondaryPhy: Int = BluetoothDevice.PHY_LE_2M
)
