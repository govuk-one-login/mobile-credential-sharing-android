package uk.gov.onelogin.sharing.bluetooth.api.permissions

import android.Manifest
import android.os.Build

/**
 * Checks if the application has the necessary permissions for Bluetooth operations.
 *
 * This contract separates the permission checks for acting as a Bluetooth Peripheral (server)
 * versus a Bluetooth Central (client).
 */
interface PermissionChecker {
    /**
     * Checks if the app has the required permissions to act as a Bluetooth Peripheral.
     * This typically includes permissions for advertising and acting as a GATT server.
     *
     * @return `true` if all required peripheral permissions are granted.
     */
    fun hasPeripheralPermissions(): Boolean

    /**
     * Checks if the app has the required permissions to act as a Bluetooth Central.
     * This typically includes permissions for scanning and connecting to GATT servers.
     *
     * @return `true` if all required central permissions are granted.
     */
    fun hasCentralPermissions(): Boolean

    companion object {
        @JvmStatic
        fun advertiseFineLocationPermissions(): List<String> = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                add(Manifest.permission.BLUETOOTH)
            }
        }

        @JvmStatic
        fun advertisePermissions(): List<String> = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            } else {
                add(Manifest.permission.BLUETOOTH)
            }
        }

        @JvmStatic
        fun centralPermissions(): List<String> = listOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Manifest.permission.BLUETOOTH_CONNECT
            } else {
                Manifest.permission.BLUETOOTH
            }
        )
    }
}
