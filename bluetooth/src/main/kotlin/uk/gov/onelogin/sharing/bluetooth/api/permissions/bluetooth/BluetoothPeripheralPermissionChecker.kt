package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.Manifest
import android.os.Build
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.Response

/**
 * Checks if the app has the required permissions to act as a Bluetooth Peripheral.
 */
fun interface BluetoothPeripheralPermissionChecker {
    /**
     * Checks if the app has the required permissions to act as a Bluetooth Peripheral.
     * This typically includes permissions for advertising and acting as a GATT server.
     *
     * @return [Response.Passed] if all required peripheral permissions are granted.
     * Otherwise, [Response.Missing] containing the list of required permissions.
     */
    fun checkPeripheralPermissions(): Response

    /**
     * Checks if the app has the required permissions to act as a Bluetooth Peripheral.
     * This typically includes permissions for advertising and acting as a GATT server.
     *
     * @return `true` if all required peripheral permissions are granted.
     */
    fun hasPeripheralPermissions(): Boolean = checkPeripheralPermissions() == Response.Passed

    companion object {
        @JvmStatic
        fun peripheralPermissions(): List<String> = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            } else {
                add(Manifest.permission.BLUETOOTH)
            }
        }
    }
}
