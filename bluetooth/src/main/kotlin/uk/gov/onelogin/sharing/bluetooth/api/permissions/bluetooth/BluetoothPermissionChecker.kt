package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

/**
 * Checks if the application has the necessary permissions for Bluetooth operations.
 *
 * This contract separates the permission checks for acting as a Bluetooth Peripheral (server)
 * versus a Bluetooth Central (client).
 */
interface BluetoothPermissionChecker :
    BluetoothPeripheralPermissionChecker,
    BluetoothCentralPermissionChecker
