package uk.gov.onelogin.sharing.bluetooth.api.permissions

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
}
