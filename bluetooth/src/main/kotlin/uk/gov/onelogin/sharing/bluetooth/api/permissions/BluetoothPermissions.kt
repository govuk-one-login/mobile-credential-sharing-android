package uk.gov.onelogin.sharing.bluetooth.api.permissions

import android.Manifest
import android.os.Build

/**
 * Checks if the application has the necessary permissions for Bluetooth operations.
 */
object BluetoothPermissions {
    @JvmStatic
    fun getBluetoothPermissions(): List<String> = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.BLUETOOTH)
        }
    }
}
