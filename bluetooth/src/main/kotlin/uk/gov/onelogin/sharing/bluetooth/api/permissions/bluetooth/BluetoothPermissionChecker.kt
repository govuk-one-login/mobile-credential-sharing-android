package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.Manifest
import android.os.Build
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.Response

/**
 * Checks if the application has the necessary permissions for Bluetooth operations.
 */
fun interface BluetoothPermissionChecker {

    /**
     * Checks if the app has the required permissions to interact with devices via Bluetooth.
     *
     * @return [Response.Passed] if all required permissions are granted.
     * Otherwise, [Response.Missing] containing the list of required permissions.
     */
    fun checkBluetoothPermissions(): Response

    /**
     * Checks if the app has the required permissions to interact with devices via Bluetooth.
     *
     * @return `true` if all required permissions are granted.
     */
    fun hasBluetoothPermissions(): Boolean = checkBluetoothPermissions() == Response.Passed

    companion object {
        @JvmStatic
        fun bluetoothPermissions(): List<String> = buildList {
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
}
