package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

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
}
