package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import uk.gov.onelogin.sharing.core.permission.PermissionChecker

/**
 * Checks if the app has the required permissions to act as a Bluetooth Central.
 */
fun interface BluetoothCentralPermissionChecker {
    /**
     * Checks if the app has the required permissions to act as a Bluetooth Central.
     * This typically includes permissions for scanning and connecting to GATT servers.
     *
     * @return [PermissionChecker.Response.Passed] if all required central permissions are granted.
     * Otherwise, [PermissionChecker.Response.Missing] containing the list of required permissions.
     */
    fun checkCentralPermissions(): PermissionChecker.Response

    /**
     * Checks if the app has the required permissions to act as a Bluetooth Central.
     * This typically includes permissions for scanning and connecting to GATT servers.
     *
     * @return `true` if all required central permissions are granted.
     */
    fun hasCentralPermissions(): Boolean =
        checkCentralPermissions() == PermissionChecker.Response.Passed
}
